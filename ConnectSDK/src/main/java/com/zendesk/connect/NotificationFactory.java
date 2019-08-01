package com.zendesk.connect;

import android.app.Notification;

/**
 * Defines an object that creates display notifications
 */
public interface NotificationFactory {

    /**
     * Creates an instance of {@link Notification} to be displayed on device
     *
     * @param payload the {@link SystemPushPayload} containing the push payload
     * @return an instance of {@link Notification} to be displayed
     */
    Notification create(SystemPushPayload payload);

}
