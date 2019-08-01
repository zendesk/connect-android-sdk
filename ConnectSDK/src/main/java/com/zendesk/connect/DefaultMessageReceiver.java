package com.zendesk.connect;

import android.app.Service;

import com.google.firebase.messaging.RemoteMessage;
import com.zendesk.logger.Logger;

import javax.inject.Inject;

/**
 * Implementation of {@link MessageReceiver} that is invoked when a non-Connect notification
 * has been received
 */
@ConnectScope
class DefaultMessageReceiver implements MessageReceiver {

    private static final String LOG_TAG = "DefaultMessageReceiver";

    @Inject
    DefaultMessageReceiver() {

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage, Service service) {
        Logger.d(LOG_TAG, "onMessageReceived has not been implemented by the integrator");
    }

    @Override
    public void onDeletedMessages(Service service) {
        Logger.d(LOG_TAG, "onDeletedMessages has not been implemented by the integrator");
    }

    @Override
    public void onMessageSent(String messageId, Service service) {
        Logger.d(LOG_TAG, "onMessageSent has not been implemented by the integrator");
    }

    @Override
    public void onSendError(String messageId, Exception exception, Service service) {
        Logger.d(LOG_TAG, "onSendError has not been implemented by the integrator");
    }

    @Override
    public void onNewToken(String token, Service service) {
        Logger.d(LOG_TAG, "onNewToken has not been implemented by the integrator");
    }

}
