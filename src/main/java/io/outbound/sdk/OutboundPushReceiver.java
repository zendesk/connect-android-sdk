package io.outbound.sdk;

import android.app.Activity;
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

    private void handleOutboundIntent(@NonNull Context context, @NonNull Intent intent) {
        // silent flag is indicator that it is an outbound notification.
        if (!intent.hasExtra("_oq")) {
            Log.e(TAG, "Received a non-Outbound notification. Ignoring.");
            return;
        }

        Bundle extras = intent.getExtras();
        if (extras == null) {
            // if there are no extras, no point in continuing.
            return;
        }

        PushNotification notif = new PushNotification(extras);

        if (!notif.isSilent()) {
            if (!extras.containsKey("_onid")) {
                Log.e(TAG, "Malformed Outbound notification. Ignoring.");
                return;
            }

            Intent notifIntent = new Intent(context.getPackageName() + OutboundService.ACTION_DISPLAY_NOTIF);
            notifIntent.setPackage(context.getApplicationContext().getPackageName());
            notifIntent.putExtra(OutboundService.EXTRA_NOTIFICATION, notif);
            startWakefulService(context, notifIntent);
        }

        if (notif.isTracker()) {
            Intent utIntent = new Intent(context.getPackageName() + OutboundService.ACTION_TRACK_NOTIF);
            utIntent.setPackage(context.getApplicationContext().getPackageName());
            utIntent.putExtra(OutboundService.EXTRA_NOTIFICATION, notif);
            startWakefulService(context, utIntent);
        }

        if (!notif.isTracker()) {
            Intent recvIntent = new Intent(context.getPackageName() + OutboundService.ACTION_RECEIVED_NOTIF);
            recvIntent.setPackage(context.getApplicationContext().getPackageName());
            recvIntent.putExtra(OutboundService.EXTRA_NOTIFICATION, notif);
            startWakefulService(context, recvIntent);
        }

        setResultCode(Activity.RESULT_OK);
    }
}