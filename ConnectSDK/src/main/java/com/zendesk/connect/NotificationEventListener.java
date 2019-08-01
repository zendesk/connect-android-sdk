package com.zendesk.connect;

/**
 * Defines callbacks that are invoked from notification events
 */
public interface NotificationEventListener {

    /**
     * Invoked when a Connect notification has been received
     *
     * @param payload the {@link SystemPushPayload} received in the push
     */
    void onNotificationReceived(SystemPushPayload payload);

    /**
     * Invoked when a Connect notification has been displayed
     *
     * @param payload the {@link SystemPushPayload} from which the display notification was created
     */
    void onNotificationDisplayed(SystemPushPayload payload);

}
