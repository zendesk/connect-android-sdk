package com.zendesk.connect;

import android.content.Intent;
import android.os.Parcelable;

import com.zendesk.logger.Logger;
import com.zendesk.util.StringUtils;

import java.io.IOException;

import static com.zendesk.connect.Connect.CLIENT_PLATFORM;
import static com.zendesk.connect.ConnectActionService.EXTRA_NOTIFICATION;

/**
 * Handles processing of {@link Intent} and metrics requests for the {@link ConnectActionService}
 */
class ConnectActionProcessor {

    private final static String LOG_TAG = "ConnectActionProcessor";

    private MetricsProvider metricsProvider;

    ConnectActionProcessor(MetricsProvider metricsProvider) {
        this.metricsProvider = metricsProvider;
    }

    /**
     * Attempts to extract a {@link NotificationPayload} from the given {@link Intent}
     *
     * @param intent the received {@link Intent}
     * @return an instance of {@link NotificationPayload} or null
     */
    NotificationPayload extractPayloadFromIntent(Intent intent) {
        Parcelable extra = intent.getParcelableExtra(EXTRA_NOTIFICATION);
        return extra instanceof NotificationPayload ? (NotificationPayload) extra : null;
    }

    /**
     * Verifies that the given intent is not null and contains the expected action
     *
     * @param intent the {@link Intent} to verify
     * @param expectedActionName the expected action
     * @return true if the action is valid, false otherwise
     */
    boolean verifyIntent(Intent intent, String expectedActionName) {
        if (intent == null) {
            return false;
        }
        String action = intent.getAction();
        return !StringUtils.isEmpty(action) && action.equals(expectedActionName);
    }

    /**
     * Sends any metrics requests to Connect to signify that a notification has been opened
     *
     * @param payload the {@link NotificationPayload} extracted from the intent
     */
    void sendMetrics(NotificationPayload payload) {
        if (payload == null) {
            Logger.e(LOG_TAG, "Payload must not be null");
            return;
        }

        String instanceId = payload.getInstanceId();
        if (StringUtils.isEmpty(instanceId)) {
            Logger.e(LOG_TAG, "Payload is not a valid Connect notification");
            return;
        }

        if (payload.isTestPush()) {
            Logger.d(LOG_TAG, "Notification is a test push, not sending metrics");
            return;
        }

        PushBasicMetric basicMetric = new PushBasicMetric(instanceId);
        try {
            metricsProvider
                    .opened(CLIENT_PLATFORM, basicMetric)
                    .execute();
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Error sending opened notification metric", e);
        }
    }
}
