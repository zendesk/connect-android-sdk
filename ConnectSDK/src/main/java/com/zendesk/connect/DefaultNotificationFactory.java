package com.zendesk.connect;

import android.app.Notification;

import com.zendesk.logger.Logger;

import javax.inject.Inject;

/**
 * Default implementation of {@link NotificationFactory} that is invoked to create custom
 * display notifications
 */
@ConnectScope
class DefaultNotificationFactory implements NotificationFactory {

    private static final String LOG_TAG = "DefaultNotificationFactory";

    @Inject
    DefaultNotificationFactory() {

    }

    @Override
    public Notification create(SystemPushPayload payload) {
        Logger.d(LOG_TAG, "Custom notification has not been provided");
        return null;
    }

}
