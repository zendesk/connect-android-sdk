package io.outbound.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.WakefulBroadcastReceiver;

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
        int result = new OutboundIntentHandler().handleIntent(context, intent);

        if (result == 0) {
            setResultCode(Activity.RESULT_OK);
        }
    }
}