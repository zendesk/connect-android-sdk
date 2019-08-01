package com.zendesk.connect;

import android.os.AsyncTask;

import androidx.annotation.VisibleForTesting;

import javax.inject.Inject;

/**
 * Processes metrics specific to {@link IpmPayload}
 */
@ConnectScope
class IpmMetricProcessor {

    @VisibleForTesting
    static final String EVENT_ACTION = "ipm_metric_action_tapped";
    @VisibleForTesting
    static final String EVENT_DISMISS = "ipm_metric_dismissed";

    private final MetricRequestsProcessor metricRequestsProcessor;
    private final ConnectClient connectClient;

    @Inject
    IpmMetricProcessor(MetricRequestsProcessor metricRequestsProcessor,
                       ConnectClient connectClient) {
        this.connectClient = connectClient;
        this.metricRequestsProcessor = metricRequestsProcessor;
    }

    /**
     * Sends requests to Connect to signify that an has been received and opened.
     * <p>
     * Both events are intentionally tracked at once to avoid a duplication of the "Received" metric
     * in case an {@link IpmPayload} falls back to a Push Notification.
     *
     * @param instanceId the unique identifier of a received payload
     */
    void trackDisplayed(final String instanceId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                metricRequestsProcessor.sendReceivedRequest(instanceId);
                metricRequestsProcessor.sendOpenedRequest(instanceId, false);
            }
        });
    }

    /**
     * Sends an event to Connect to signify that an {@link IpmPayload} action button has been
     * pressed by the user
     */
    void trackAction() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                connectClient.trackEvent(createEvent(EVENT_ACTION));
            }
        });
    }

    /**
     * Sends an event to Connect to signify that an {@link IpmPayload} was dismissed by the
     * user
     */
    void trackDismiss() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                connectClient.trackEvent(createEvent(EVENT_DISMISS));
            }
        });
    }

    /**
     * Creates an {@link Event} using the {@link EventFactory}
     *
     * @param eventName the name of the event
     * @return a constructed {@link Event} object
     */
    @VisibleForTesting
    Event createEvent(String eventName) {
        return EventFactory.createEvent(eventName);
    }
}
