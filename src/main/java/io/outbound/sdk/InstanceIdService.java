package io.outbound.sdk;

import com.google.android.gms.iid.InstanceIDListenerService;

public class InstanceIdService extends InstanceIDListenerService{
    public void onTokenRefresh() {
        OutboundClient.getInstance().refreshGcmToken();
    }
};