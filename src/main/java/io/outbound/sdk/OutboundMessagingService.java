package io.outbound.sdk;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;


public class OutboundMessagingService extends FirebaseMessagingService {
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

    public void onReceivedMessage(RemoteMessage message) { }

    private void handleOutboundMessage(RemoteMessage message) {
        Bundle bundle = new Bundle();
        for (Map.Entry<String,String> entry : message.getData().entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }

        Intent intent = new Intent();
        intent.putExtras(bundle);
        new OutboundIntentHandler().handleIntent(this, intent);
    }
}
