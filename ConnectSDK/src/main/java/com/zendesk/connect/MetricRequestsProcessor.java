package com.zendesk.connect;

import android.support.annotation.VisibleForTesting;
import android.support.v4.app.NotificationManagerCompat;

import com.zendesk.logger.Logger;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

import static com.zendesk.connect.Connect.CLIENT_PLATFORM;

class MetricRequestsProcessor {

    private final static String LOG_TAG = "MetricRequestsProcessor";

    private MetricsProvider metricsProvider;

    MetricRequestsProcessor(MetricsProvider metricsProvider) {
        this.metricsProvider = metricsProvider;
    }

    /**
     * Sends a request to Connect to signify that a notification has been opened
     *
     * @param payload the {@link NotificationPayload} extracted from the intent
     */
    void sendOpenedRequest(NotificationPayload payload) {
        if (payload == null) {
            Logger.e(LOG_TAG, "Payload must not be null");
            return;
        }

        String instanceId = payload.getInstanceId();
        if (instanceId == null) {
            Logger.e(LOG_TAG, "Payload is not a valid Connect notification");
            return;
        }

        if (payload.isTestPush()) {
            Logger.d(LOG_TAG, "Notification is a test push, not sending metrics");
            return;
        }

        PushBasicMetric basicMetric = new PushBasicMetric(instanceId);

        Call<Void> call = metricsProvider.opened(CLIENT_PLATFORM, basicMetric);

        sendRequest(call);
    }

    /**
     * Sends a request to Connect to signify that a notification has been received
     *
     * @param payload the {@link NotificationPayload} extracted from the notification
     */
    void sendReceivedRequest(NotificationPayload payload) {
        PushBasicMetric basicMetric = new PushBasicMetric(payload.getInstanceId());

        Call<Void> receivedRequest = metricsProvider
                .received(CLIENT_PLATFORM, basicMetric);
        sendRequest(receivedRequest);
    }

    /**
     * Sends an uninstall tracker request to the Connect backend
     *
     * @param payload the {@link NotificationPayload} extracted from the notification
     * @param notificationsRevoked true if the user has disabled notifications
     */
    void sendUninstallTrackerRequest(NotificationPayload payload,
                                     boolean notificationsRevoked) {
        UninstallTracker uninstallTracker =
                new UninstallTracker(payload.getInstanceId(), notificationsRevoked);

        Call<Void> uninstallTrackerRequest = metricsProvider
                .uninstallTracker(CLIENT_PLATFORM, uninstallTracker);
        sendRequest(uninstallTrackerRequest);
    }

    /**
     * Sends the given request. Wraps the execution in a try catch here so we don't have to
     * do it multiple times elsewhere in the class
     *
     * @param call the {@link Call} object to be executed
     */
    @VisibleForTesting
    void sendRequest(Call<Void> call) {
        if (call == null) {
            Logger.e(LOG_TAG, "Call was null, couldn't send request");
            return;
        }

        try {
            Response<Void> response = call.execute();
            if (!response.isSuccessful()) {
                Logger.d(LOG_TAG, "Metric request unsuccessful. Response code:", response.code());
            }
        } catch (IOException e) {
            Logger.e(LOG_TAG, "There was a problem sending the metric request:", e);
        }
    }
}
