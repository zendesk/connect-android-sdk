package io.outbound.sdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * OutboundMessagingService handles incoming push notifications from Firebase.
 */
public class OutboundMessagingService extends FirebaseMessagingService {
    public final static String TAG = BuildConfig.APPLICATION_ID;

    @Override
    public void onMessageReceived(RemoteMessage message) {
        if (message == null || message.getData() == null) {
            return;
        }

        Map<String,String> data = message.getData();
        if (data.containsKey("_oq")) {
            handleOutboundMessage(message);
        } else {
            onReceivedMessage(message);
        }
    }

    /**
     * Called when an unhandled non-Outbound notification is received. This method is recommended
     * for custom handling of your own notifications.
     *
     * @param message
     */
    public void onReceivedMessage(RemoteMessage message) { }

    /**
     * Called after an Outbound notification is received. Called for <b>ALL</b> notifications whether
     * they display or not. This method is recommended for handling any custom payload properties
     * sent in the notification.
     *
     * @param notification
     */
    public void onNotificationReceived(PushNotification notification) { }

    /**
     * Called after an Outbound notification is displayed. <b>IS NOT</b> called for silent notifications
     * since they do not "display."
     *
     * @param notification
     */
    public void onNotificationDisplayed(PushNotification notification) { }

    /**
     * Called to create a {@link Notification} from a {@link PushNotification}.
     * This can be overridden to allow custom styling not provided by default through the SDK.
     *
     * @param notification
     */
    public Notification buildNotification(PushNotification notification) {
        return notification.createNotificationBuilder(this).build();
    }

    private void handleOutboundMessage(RemoteMessage message) {
        Bundle bundle = new Bundle();
        for (Map.Entry<String,String> entry : message.getData().entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }

        PushNotification outboundNotification = new PushNotification(bundle);
        if (!outboundNotification.isSilent()) {
            if (!bundle.containsKey("_onid")) {
                Log.e(TAG, "Malformed Outbound notification. Ignoring.");
                return;
            }

            // Display notification
            Notification notification = buildNotification(outboundNotification);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(outboundNotification.getId(), notification);

            onNotificationDisplayed(outboundNotification);
        }

        if (outboundNotification.isUninstallTracker()) {
            new OutboundJobScheduler(this).scheduleUninstallTrack(outboundNotification);
        } else {
            new OutboundJobScheduler(this).scheduleNotificationReceived(outboundNotification);
        }

        onNotificationReceived(outboundNotification);
    }
}
