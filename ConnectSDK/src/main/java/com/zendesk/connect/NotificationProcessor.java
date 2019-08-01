package com.zendesk.connect;

import com.zendesk.logger.Logger;

import java.util.Map;

import javax.inject.Inject;

/**
 * Processor class responsible for processing a received Connect notification payload
 */
@ConnectScope
class NotificationProcessor {

    private static final String LOG_TAG = "NotificationProcessor";

    private static NotificationEventListener notificationEventListener;
    private static NotificationFactory notificationFactory;

    private StubPushStrategyFactory stubPushStrategyFactory;
    private SystemPushStrategyFactory systemPushStrategyFactory;
    private IpmPushStrategyFactory ipmPushStrategyFactory;

    /**
     * Creates an instance of this {@link NotificationProcessor}
     *
     * @param stubPushStrategyFactory an instance of {@link StubPushStrategyFactory}
     * @param systemPushStrategyFactory an instance of {@link SystemPushStrategyFactory}
     * @param ipmPushStrategyFactory an instance of {@link IpmPushStrategyFactory}
     */
    @Inject
    NotificationProcessor(StubPushStrategyFactory stubPushStrategyFactory,
                          SystemPushStrategyFactory systemPushStrategyFactory,
                          IpmPushStrategyFactory ipmPushStrategyFactory,
                          NotificationEventListener notificationEventListener,
                          NotificationFactory notificationFactory) {

        this.stubPushStrategyFactory = stubPushStrategyFactory;
        this.systemPushStrategyFactory = systemPushStrategyFactory;
        this.ipmPushStrategyFactory = ipmPushStrategyFactory;

        if (NotificationProcessor.notificationEventListener == null) {
            NotificationProcessor.notificationEventListener = notificationEventListener;
        }

        if (NotificationProcessor.notificationFactory == null) {
            NotificationProcessor.notificationFactory = notificationFactory;
        }
    }

    /**
     * Sets an implementation of {@link NotificationEventListener} to be invoked when notification events occur
     *
     * @param notificationEventListener an implementation of {@link NotificationEventListener}
     */
    static void setNotificationEventListener(NotificationEventListener notificationEventListener) {
        NotificationProcessor.notificationEventListener = notificationEventListener;
    }

    /**
     * Sets an implementation of {@link NotificationFactory} to be invoked when a display
     * notification is being created.
     *
     * @param notificationFactory an implementation of {@link NotificationFactory}
     */
    static void setNotificationFactory(NotificationFactory notificationFactory) {
        NotificationProcessor.notificationFactory = notificationFactory;
    }

    /**
     * Processes the given payload by:
     * <li>Determining the payload type</li>
     * <li>Querying the appropriate strategy factory</li>
     * <li>Invoking the created {@link PushStrategy}</li>
     *
     * @param data the JSON dictionary received in the push payload
     */
    void process(Map<String, String> data) {

        PushStrategy pushStrategy;
        ConnectNotification.Types type = ConnectNotification.getNotificationType(data);
        switch (type) {
            case SYSTEM_PUSH:
                pushStrategy = systemPushStrategyFactory.create(notificationEventListener, notificationFactory);
                break;
            case IPM:
                pushStrategy = ipmPushStrategyFactory.create();
                break;
            case UNKNOWN:
            default:
                Logger.w(LOG_TAG, "Couldn't create push strategy for %s payload", type);
                pushStrategy = stubPushStrategyFactory.create();
                break;
        }
        pushStrategy.process(data);
    }

}
