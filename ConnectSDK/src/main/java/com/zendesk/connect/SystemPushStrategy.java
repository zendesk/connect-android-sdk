package com.zendesk.connect;

import android.app.Notification;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.NotificationCompat;

import com.zendesk.logger.Logger;
import com.zendesk.util.StringUtils;

import java.util.Map;

/**
 * Implementation of {@link PushStrategy} used for processing and displaying basic system push notifications
 */
class SystemPushStrategy implements PushStrategy {

    private static final String LOG_TAG = "SystemPushStrategy";

    private NotificationManager notificationManager;
    private NotificationBuilder notificationBuilder;
    private MetricRequestsProcessor metricsProcessor;
    private NotificationEventListener notificationEventListener;
    private NotificationFactory notificationFactory;
    private SystemPushPayloadParser payloadParser;

    /**
     * Constructs an instance of this push strategy
     *
     * @param notificationManager the {@link NotificationManager} used for displaying the push
     * @param notificationBuilder the {@link NotificationBuilder} for constructing display notifications
     * @param metricsProcessor the {@link MetricRequestsProcessor} for sending metrics requests
     * @param notificationEventListener the {@link NotificationEventListener} provided by the integrator
     * @param notificationFactory the {@link NotificationFactory} provided by the integrator
     * @param payloadParser an instance of {@link SystemPushPayloadParser}
     */
    SystemPushStrategy(NotificationManager notificationManager,
                       NotificationBuilder notificationBuilder,
                       MetricRequestsProcessor metricsProcessor,
                       NotificationEventListener notificationEventListener,
                       NotificationFactory notificationFactory,
                       SystemPushPayloadParser payloadParser) {
        this.notificationManager = notificationManager;
        this.notificationBuilder = notificationBuilder;
        this.metricsProcessor = metricsProcessor;
        this.notificationEventListener = notificationEventListener;
        this.notificationFactory = notificationFactory;
        this.payloadParser = payloadParser;
    }

    @Override
    public void process(Map<String, String> data) {
        SystemPushPayload payload = payloadParser.parse(data);

        if (!payload.isQuietPush()) {
            displayNotification(payload);
        }

        sendMetrics(payload, metricsProcessor, notificationManager);

        notificationEventListener.onNotificationReceived(payload);
    }

    /**
     * Displays the notification to the user, and calls the {@link NotificationEventListener
     * #onNotificationDisplayed(SystemPushPayload)} method for the integrator to handle
     *
     * @param payload the {@link SystemPushPayload} parsed from the received notification
     */
    @VisibleForTesting
    void displayNotification(SystemPushPayload payload) {
        Notification notification = notificationFactory.create(payload);
        if (notification == null) {
            // If the custom provided notification is null, we fall back to our own implementation
            notification = buildConnectNotification(payload, notificationBuilder);
        }

        if (notification != null) {
            notificationManager.notify(payload.getNotificationId(), notification);
            notificationEventListener.onNotificationDisplayed(payload);
        } else {
            Logger.e(LOG_TAG, "Unable to build notification to display");
        }
    }

    /**
     * Constructs a {@link Notification} from the {@link SystemPushPayload} payload received
     *
     * @param data the {@link SystemPushPayload} parsed from the push payload
     * @param notificationBuilder an instance of {@link NotificationBuilder} for creating display notifications
     * @return a notification to be displayed, or null if an issue was encountered
     */
    @Nullable
    @VisibleForTesting
    Notification buildConnectNotification(SystemPushPayload data, NotificationBuilder notificationBuilder) {
        if (data == null) {
            Logger.e(LOG_TAG, "Payload data was null, unable to create notification");
            return null;
        }

        String title = data.getTitle();
        String body = data.getBody();

        notificationBuilder.setTitle(StringUtils.ensureEmpty(title))
                .setBody(StringUtils.ensureEmpty(body))
                .setAutoCancel(true)
                .setLocalOnly(true);

        if (body != null) {
            NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
            style.bigText(body);
            notificationBuilder.setStyle(style);
        }

        String smallImageFile = data.getSmallNotificationImagePath();
        String smallImageFolder = data.getSmallNotificationFolderPath();
        notificationBuilder.setSmallIcon(smallImageFile,
                smallImageFolder,
                R.drawable.ic_connect_notification_icon);

        String largeImageFile = data.getLargeNotificationImagePath();
        String largeImageFolder = data.getLargeNotificationFolderPath();
        if (StringUtils.hasLengthMany(largeImageFile, largeImageFolder)) {
            notificationBuilder.setLargeIcon(largeImageFile, largeImageFolder);
        } else {
            Logger.w(LOG_TAG, "Large icon doesn't exist, there will be no large icon");
        }

        if (data.isSilent()) {
            notificationBuilder.setSilent();
        }

        String category = data.getCategory();
        if (category != null) {
            notificationBuilder.setCategory(category);
        }

        notificationBuilder.setPendingIntent(data);

        return notificationBuilder.build();
    }

    /**
     * Sends the necessary metrics for the received notification
     *
     * @param payload the {@link SystemPushPayload} parsed from the received notification
     * @param metricsProcessor an instance of {@link MetricRequestsProcessor} to handle sending metrics requests
     * @param notificationManager an instance of {@link NotificationManager}
     */
    @VisibleForTesting
    void sendMetrics(SystemPushPayload payload,
                     MetricRequestsProcessor metricsProcessor,
                     NotificationManager notificationManager) {
        if (metricsProcessor == null) {
            Logger.e(LOG_TAG, "Metrics processor was null, unable to send metrics");
            return;
        }

        if (payload.isUninstallTracker()) {
            boolean revoked = !notificationManager.areNotificationsEnabled();
            metricsProcessor.sendUninstallTrackerRequest(payload.getInstanceId(), revoked);
        } else {
            metricsProcessor.sendReceivedRequest(payload.getInstanceId());
        }
    }

}
