package com.zendesk.connect;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.zendesk.logger.Logger;

/**
 * Service class to receive push notifications from Firebase. Connect will attempt to handle
 * the push payload and invoke:
 * <li>
 *     {@link MessageReceiver} to control non-Connect messages, and pass along other Firebase events
 * </li>
 * <li>
 *     {@link NotificationEventListener} to react to notification events
 * </li>
 * <li>
 *     {@link NotificationFactory} to create custom display notifications.
 * </li>
 */
public class ConnectMessagingService extends FirebaseMessagingService {

    private static final String LOG_TAG = "ConnectMessagingService";

    private static MessageReceiver messageReceiver;

    public ConnectMessagingService() {
        if (messageReceiver == null) {
            messageReceiver = new DefaultMessageReceiver();
        }
    }

    @Override
    public final void onMessageReceived(RemoteMessage message) {
        if (message == null || message.getData() == null) {
            Logger.e(LOG_TAG, "Push Notification contained no data");
            return;
        }

        if (!ConnectNotification.isConnectPush(message.getData())) {
            Logger.d(LOG_TAG, "Message is not a connect push");
            messageReceiver.onMessageReceived(message, this);
            return;
        }

        ConnectComponent connectComponent = Connect.INSTANCE.getComponent();
        if (connectComponent == null) {
            Logger.e(LOG_TAG, "Connect has not been initialised, couldn't handle notification");
            return;
        }

        NotificationProcessor notificationProcessor = connectComponent.notificationProcessor();
        notificationProcessor.process(message.getData());
    }

    @Override
    public final void onDeletedMessages() {
        messageReceiver.onDeletedMessages(this);
    }

    @Override
    public final void onMessageSent(String s) {
        messageReceiver.onMessageSent(s, this);
    }

    @Override
    public final void onSendError(String s, Exception e) {
        messageReceiver.onSendError(s, e, this);
    }

    @Override
    public final void onNewToken(String s) {
        messageReceiver.onNewToken(s, this);
    }

    /**
     * Sets an implementation of {@link MessageReceiver} to be invoked when a non-Connect
     * {@link RemoteMessage} is received.
     *
     * @param messageReceiver an implementation of {@link MessageReceiver}
     */
    public static void setMessageReceiver(MessageReceiver messageReceiver) {
        ConnectMessagingService.messageReceiver = messageReceiver;
    }

    /**
     * Sets an implementation of {@link NotificationEventListener} to be invoked when notification events occur
     *
     * @param notificationEventListener an implementation of {@link NotificationEventListener}
     */
    public static void setNotificationEventListener(NotificationEventListener notificationEventListener) {
        NotificationProcessor.setNotificationEventListener(notificationEventListener);
    }

    /**
     * Sets an implementation of {@link NotificationFactory} to be invoked when a display
     * notification is being created.
     *
     * @param notificationFactory an implementation of {@link NotificationFactory}
     */
    public static void setNotificationFactory(NotificationFactory notificationFactory) {
        NotificationProcessor.setNotificationFactory(notificationFactory);
    }
}
