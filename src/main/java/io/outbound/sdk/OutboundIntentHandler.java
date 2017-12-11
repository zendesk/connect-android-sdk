package io.outbound.sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

class OutboundIntentHandler {
    public final static String TAG = BuildConfig.APPLICATION_ID;

    public int handleIntent(Context context, Intent intent) {
        // silent flag is indicator that it is an outbound notification.
        if (!intent.hasExtra("_oq")) {
            Log.e(TAG, "Received a non-Outbound notification. Ignoring.");
            return -1;
        }

        Bundle extras = intent.getExtras();
        if (extras == null) {
            // if there are no extras, no point in continuing.
            return -2;
        }

        PushNotification notif = new PushNotification(extras);
        if (notif.isTracker()) {
            Intent utIntent = new Intent(context.getPackageName() + OutboundService.ACTION_TRACK_NOTIF);
            utIntent.setPackage(context.getApplicationContext().getPackageName());
            utIntent.putExtra(OutboundService.EXTRA_NOTIFICATION, notif);
            WakefulBroadcastReceiver.startWakefulService(context, utIntent);
        } else {
            Intent recvIntent = new Intent(context.getPackageName() + OutboundService.ACTION_RECEIVED_NOTIF);
            recvIntent.setPackage(context.getApplicationContext().getPackageName());
            recvIntent.putExtra(OutboundService.EXTRA_NOTIFICATION, notif);
            WakefulBroadcastReceiver.startWakefulService(context, recvIntent);
        }

        return 0;
    }
}
