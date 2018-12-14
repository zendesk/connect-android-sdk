package com.zendesk.connect;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.zendesk.logger.Logger;

/**
 * <p>
 *     Handles incoming notifications from Firebase. The SDK will attempt to parse the message
 *     received into a {@link NotificationPayload} object which represents the data of a Connect
 *     notification. This service should be registered in the Android manifest of the host app.
 * </p>
 *
 * <pre>{@code
 *     <service android:name="com.zendesk.connect.ConnectMessagingService">
 *         <intent-filter>
 *             <action android:name="com.google.firebase.MESSAGING_EVENT"/>
 *         </intent-filter>
 *     </service>
 * }</pre>
 *
 * <p>
 *     By default the SDK will build and display notifications received from Connect but will
 *     not do anything for notifications received from other external sources. To handle any
 *     other pushes you should extend this class and implement the public methods as needed.
 * </p>
 *
 * <ul>
 *     <li>
 *         {@link #handleNonConnectNotification(RemoteMessage)} is called when a notification
 *         is received that didn't originate from Connect. Should be overridden to handle
 *         displaying your own push notifications.
 *     </li>
 *     <li>
 *         {@link #onNotificationReceived(NotificationPayload)} is called when a Connect
 *         notification is received, regardless of whether it is displayed or not. Should
 *         be overridden to handle custom data contained in the push payload.
 *     </li>
 *     <li>
 *         {@link #onNotificationDisplayed(NotificationPayload)} is called when a Connect
 *         notification has been displayed to the user.
 *     </li>
 *     <li>
 *         {@link #provideCustomNotification(NotificationPayload)} is used to construct a
 *         {@link Notification} to display to the user when a Connect notification is received.
 *         Should be overridden to handle building the {@link Notification} yourself.
 *     </li>
 * </ul>
 */
public class ConnectMessagingService extends FirebaseMessagingService {

    private final static String LOG_TAG = "ConnectMessagingService";

    private NotificationProcessor notificationProcessor;

    public ConnectMessagingService() {
        this.notificationProcessor = null;
    }

    @Override
    public final void onMessageReceived(RemoteMessage message) {
        if (message == null || message.getData() == null) {
            Logger.d(LOG_TAG, "Push Notification contained no data");
            return;
        }

        notificationProcessor = Connect.INSTANCE.notificationProcessor();
        if (notificationProcessor == null) {
            Logger.d(LOG_TAG, "Notification processor was null, couldn't handle notification");
            return;
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            Logger.d(LOG_TAG, "Notification Manager was null");
            return;
        }

        NotificationPayload payload = notificationProcessor.parseRemoteMessage(message);
        if (payload == null || !payload.isConnectNotification()) {
            handleNonConnectNotification(message);
            return;
        }

        displayNotification(payload, manager);

        MetricRequestsProcessor metricsProcessor = Connect.INSTANCE.metricsProcessor();
        sendMetrics(payload, metricsProcessor);

        onNotificationReceived(payload);
    }

    /**
     * Displays the notification to the user, and calls the {@link #onNotificationDisplayed(NotificationPayload)}
     * method for the integrator to handle
     *
     * @param payload The {@link NotificationPayload} parsed from the received notification
     * @param manager The {@link NotificationManager} used to display the notification
     */
    private void displayNotification(NotificationPayload payload, NotificationManager manager) {
        if (!payload.isQuietPush()) {
            Notification notification = provideCustomNotification(payload);
            if (notification == null) {
                // If the custom provided notification is null, we fall back to our own implementation
                notification = notificationProcessor.buildConnectNotification(payload);
            }

            if (notification != null) {
                manager.notify(payload.getNotificationId(), notification);
                onNotificationDisplayed(payload);
            } else {
                Logger.e(LOG_TAG, "Unable to build notification to display");
            }
        }
    }

    /**
     * Sends the necessary metrics for the received notification
     *
     * @param payload the {@link NotificationPayload} parsed from the received notification
     * @param metricsProcessor an instance of {@link MetricRequestsProcessor} to handle sending metrics requests
     */
    private void sendMetrics(NotificationPayload payload, MetricRequestsProcessor metricsProcessor) {
        if (metricsProcessor != null) {
            if (payload.isUninstallTracker()) {
                NotificationManagerCompat compatManager = NotificationManagerCompat.from(this);
                boolean revoked = !compatManager.areNotificationsEnabled();
                metricsProcessor.sendUninstallTrackerRequest(payload, revoked);
            } else {
                metricsProcessor.sendReceivedRequest(payload);
            }
        } else {
            Logger.d(LOG_TAG, "Metrics processor was null, unable to send metrics");
        }
    }

    /**
     * Called when a non-Connect notification is received that we will not handle ourselves. This
     * method can be overridden to allow integrators to handle their own push notifications.
     *
     * @param message the {@link RemoteMessage} received from the push notification
     */
    public void handleNonConnectNotification(RemoteMessage message) {
        Logger.d(LOG_TAG, "handleNonConnectNotification has not been implemented by the integrator");
    }

    /**
     * Called after a Connect notification is received regardless of whether they display or not.
     * This method can be overridden to handle custom payload properties.
     *
     * @param payload the {@link NotificationPayload} created from the push received
     */
    public void onNotificationReceived(NotificationPayload payload) {
        Logger.d(LOG_TAG, "onNotificationReceived has not been implemented by the integrator");
    }

    /**
     * Called after a Connect notification is displayed.
     *
     * @param payload the {@link NotificationPayload} created from the push received
     */
    public void onNotificationDisplayed(NotificationPayload payload) {
        Logger.d(LOG_TAG, "onNotificationDisplayed has not been implemented by the integrator");
    }

    /**
     * Build the {@link Notification} to be displayed
     *
     * @param payload the {@link NotificationPayload} parsed from the notification received
     * @return a constructed {@link Notification}
     */
    public Notification provideCustomNotification(NotificationPayload payload) {
        Logger.d(LOG_TAG, "Custom notification has not been provided");
        return null;
    }

}
