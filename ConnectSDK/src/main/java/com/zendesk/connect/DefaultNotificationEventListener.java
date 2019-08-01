package com.zendesk.connect;

import com.zendesk.logger.Logger;

import javax.inject.Inject;

/**
 * Implementation of {@link NotificationEventListener} that is invoked when notification events occur
 */
@ConnectScope
class DefaultNotificationEventListener implements NotificationEventListener {

    private static final String LOG_TAG = "DefaultNotificationEventListener";

    @Inject
    DefaultNotificationEventListener() {

    }

    @Override
    public void onNotificationReceived(SystemPushPayload payload) {
        Logger.d(LOG_TAG, "onNotificationReceived has not been implemented by the integrator");
    }

    @Override
    public void onNotificationDisplayed(SystemPushPayload payload) {
        Logger.d(LOG_TAG, "onNotificationDisplayed has not been implemented by the integrator");
    }

}
