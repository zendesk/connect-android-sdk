package com.zendesk.connect;

import android.app.Service;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Receiver that acts as a pass-through for non-Connect {@link RemoteMessage}s, allowing an
 * integrator to handle their own push payloads.
 */
public interface MessageReceiver {

    /**
     * Invoked when a {@link RemoteMessage} is received that does not belong to a Connect push.
     *
     * @param remoteMessage the {@link RemoteMessage} received in the push payload
     * @param service the {@link Service} context from which the message was received
     */
    void onMessageReceived(RemoteMessage remoteMessage, Service service);

    /**
     * Called when the FCM server deletes pending messages.
     *
     * @param service the {@link Service} context from which this call was made
     */
    void onDeletedMessages(Service service);

    /**
     * Called when an upstream message has been successfully sent.
     *
     * @param messageId the id of the message sent upstream
     * @param service the {@link Service} context from which this call was made
     */
    void onMessageSent(String messageId, Service service);

    /**
     * Called when there was an error sending an upstream message.
     *
     * @param messageId the id of the message being sent upstream
     * @param exception the exception that occurred
     * @param service the {@link Service} context from which this call was made
     */
    void onSendError(String messageId, Exception exception, Service service);

    /**
     * Called when a new token for the default Firebase project is generated.
     *
     * @param token the new device token
     * @param service the {@link Service} context from which this call was made
     */
    void onNewToken(String token, Service service);

}
