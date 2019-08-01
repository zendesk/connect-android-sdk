package com.zendesk.connect;

import javax.inject.Inject;

/**
 * Simple factory responsible for creating instances of {@link SystemPushStrategy}
 */
@ConnectScope
class SystemPushStrategyFactory {

    private final NotificationManager notificationManager;
    private final NotificationBuilder notificationBuilder;
    private final MetricRequestsProcessor metricsProcessor;
    private final SystemPushPayloadParser payloadParser;

    private PushStrategy systemPushStrategy;

    /**
     * Creates an instance of this {@link SystemPushStrategyFactory}
     *
     * @param notificationManager an instance of {@link NotificationManager}
     * @param notificationBuilder an instance of {@link NotificationBuilder}
     * @param metricsProcessor an instance of {@link MetricRequestsProcessor}
     * @param payloadParser an instance of {@link SystemPushPayloadParser}
     */
    @Inject
    SystemPushStrategyFactory(NotificationManager notificationManager,
                              NotificationBuilder notificationBuilder,
                              MetricRequestsProcessor metricsProcessor,
                              SystemPushPayloadParser payloadParser) {
        this.notificationManager = notificationManager;
        this.notificationBuilder = notificationBuilder;
        this.metricsProcessor = metricsProcessor;
        this.payloadParser = payloadParser;
    }

    /**
     * Creates an instance of {@link SystemPushStrategy}. Caches the created instance so subsequent
     * calls will return the same instance.
     *
     * @param notificationEventListener an instance of {@link NotificationEventListener}
     * @param notificationFactory an instance of {@link NotificationFactory}
     * @return an instance of {@link SystemPushStrategy}
     */
    PushStrategy create(NotificationEventListener notificationEventListener,
                        NotificationFactory notificationFactory) {
        if (systemPushStrategy == null) {
            systemPushStrategy = new SystemPushStrategy(
                    notificationManager,
                    notificationBuilder,
                    metricsProcessor,
                    notificationEventListener,
                    notificationFactory,
                    payloadParser
            );
        }
        return systemPushStrategy;
    }

}
