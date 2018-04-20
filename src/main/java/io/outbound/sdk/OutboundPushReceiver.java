package io.outbound.sdk;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * OutboundPushReceiver handle incoming push notifications. OutboundPushReceiver should be registered
 * as a service in your AndroidManifest.xml file.
 *
 * <p></p>
 *
 * <pre>
 * {@code
 * <receiver
 *      android:name="io.outbound.sdk.OutboundPushReceiver"
 *      android:permission="com.google.android.c2dm.permission.SEND" >
 *      <intent-filter>
 *          <action android:name="com.google.android.c2dm.intent.RECEIVE" />
 *      </intent-filter>
 * </receiver>}
 * </pre>
 *
 * <p>You may still wish to receive push notifications outside of Outbound. If you do need to handle
 * non-Outbound notifications, you can extend OutboundPushReceiver and implement {@link #onReceiveIntent(Context, Intent)}
 * method. When OutboundPushReceiver receives an intent, once it has determined that it <b>IS NOT</b>
 * an Outbound notification, it will be passed to onReceiveIntent.</p>
 */
public class OutboundPushReceiver extends WakefulBroadcastReceiver {
    public final static String TAG = BuildConfig.APPLICATION_ID;

    @Override
    public final void onReceive(@NonNull Context context, @NonNull Intent intent) {
        boolean isOutbound = false;

        // silent flag is indicator that it is an outbound notification.
        if (intent.hasExtra("_oq")) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                isOutbound = true;
                handleOutboundIntent(context, intent);
            }
        }

        if (!isOutbound) {
            onReceiveIntent(context, intent);
        }
    }

    /**
     * onReceiveIntent is called when the receiver receives an intent that is NOT for an Outbound
     * notification.
     *
     * @param context
     * @param intent
     */
    public void onReceiveIntent(@NonNull Context context, @NonNull Intent intent) {}
    
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
    public Notification buildNotification(Context context, PushNotification notification) {
        return notification.createNotificationBuilder(context).build();
    }

    private void handleOutboundIntent(@NonNull Context context, @NonNull Intent intent) {
        Bundle bundle = intent.getExtras();
        PushNotification outboundNotification = new PushNotification(bundle);
        if (!outboundNotification.isSilent()) {
            if (!bundle.containsKey("_onid")) {
                Log.e(TAG, "Malformed Outbound notification. Ignoring.");
                return;
            }

            // Display notification
            Notification notification = buildNotification(context, outboundNotification);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(outboundNotification.getId(), notification);

            onNotificationDisplayed(outboundNotification);
        }

        OutboundClient outboundClient = OutboundClient.getInstance();
        if (outboundNotification.isUninstallTracker()) {
            outboundClient.trackNotification(context, outboundNotification.getInstanceId());
        } else {
            outboundClient.receiveNotification(outboundNotification.getInstanceId());
        }

        onNotificationReceived(outboundNotification);

        setResultCode(Activity.RESULT_OK);
        completeWakefulIntent(intent);
    }
}