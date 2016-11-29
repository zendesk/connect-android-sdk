package io.outbound.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

public class ConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        Log.i(BuildConfig.APPLICATION_ID, "Connected status changed.");
        OutboundClient.getInstance().updateConnectionStatus();
    }
}