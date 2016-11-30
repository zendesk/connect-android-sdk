package io.outbound.sdk;

import com.google.firebase.iid.FirebaseInstanceIdService;
public class InstanceIdService extends FirebaseInstanceIdService {
    public void onTokenRefresh() {
        OutboundClient.getInstance().refreshFCMToken();
    }
};