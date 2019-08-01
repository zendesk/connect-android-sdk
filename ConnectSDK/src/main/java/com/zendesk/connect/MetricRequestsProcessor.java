package com.zendesk.connect;

import androidx.annotation.VisibleForTesting;

import com.zendesk.logger.Logger;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Response;

import static com.zendesk.connect.Connect.CLIENT_PLATFORM;

/**
 * Processes any metrics requests sent by the SDK
 */
@ConnectScope
class MetricRequestsProcessor {

    private static final String LOG_TAG = "MetricRequestsProcessor";

    private MetricsProvider metricsProvider;

    @Inject
    MetricRequestsProcessor(MetricsProvider metricsProvider) {
        this.metricsProvider = metricsProvider;
    }

    /**
     * Sends a request to Connect to signify that a notification has been opened
     *
     * @param instanceId the unique identifier of a received payload
     * @param isTestPush if the received payload is a test payload or not
     */
    void sendOpenedRequest(String instanceId, boolean isTestPush) {
        if (isTestPush) {
            Logger.d(LOG_TAG, "Notification is a test push, not sending metrics");
            return;
        }

        if (instanceId == null) {
            Logger.e(LOG_TAG, "Payload is not a valid Connect notification");
            return;
        }

        PushBasicMetric basicMetric = new PushBasicMetric(instanceId);

        Call<Void> call = metricsProvider.opened(CLIENT_PLATFORM, basicMetric);

        sendRequest(call);
    }

    /**
     * Sends a request to Connect to signify that a notification has been received
     *
     * @param instanceId the unique identifier of a received payload
     */
    void sendReceivedRequest(String instanceId) {
        if (instanceId == null) {
            Logger.e(LOG_TAG, "Payload is not a valid Connect notification");
            return;
        }

        PushBasicMetric basicMetric = new PushBasicMetric(instanceId);

        Call<Void> receivedRequest = metricsProvider.received(CLIENT_PLATFORM, basicMetric);
        sendRequest(receivedRequest);
    }

    /**
     * Sends an uninstall tracker request to the Connect backend
     *
     * @param instanceId the unique identifier of a received payload
     * @param notificationsRevoked true if the user has disabled notifications
     */
    void sendUninstallTrackerRequest(String instanceId, boolean notificationsRevoked) {
        UninstallTracker uninstallTracker = new UninstallTracker(instanceId, notificationsRevoked);

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
                Logger.e(LOG_TAG, "Metric request unsuccessful. Response code: %s", response.code());
            } else {
                Logger.d(LOG_TAG, "Metric request successful");
            }
        } catch (IOException e) {
            Logger.e(LOG_TAG, "There was a problem sending the metric request:", e);
        }
    }
}
