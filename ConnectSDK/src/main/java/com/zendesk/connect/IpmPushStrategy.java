package com.zendesk.connect;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.zendesk.logger.Logger;

import java.util.Map;

/**
 * An implementation of {@link PushStrategy} for processing and displaying incoming IPM payloads
 */
class IpmPushStrategy implements PushStrategy {

    private static final String LOG_TAG = "IpmPushStrategy";

    private final IpmPayloadParser payloadParser;
    private final Context context;
    private final IntentBuilder intentBuilder;

    /**
     * Creates an instance of {@link IpmPushStrategy}
     *
     * @param payloadParser an instance of {@link IpmPayloadParser}
     */
    IpmPushStrategy(IpmPayloadParser payloadParser,
                    Context context,
                    IntentBuilder intentBuilder) {
        this.payloadParser = payloadParser;
        this.context = context;
        this.intentBuilder = intentBuilder;
    }

    @Override
    public void process(Map<String, String> data) {
        IpmPayload ipmPayload = payloadParser.parse(data);
        if (ipmPayload == null) {
            Logger.e(LOG_TAG, "Unable to parse IPM");
            return;
        }

        intentBuilder.withExtra(ConnectIpmService.IPM_PAYLOAD_PARCELABLE_KEY, ipmPayload);

        enqueueWork(intentBuilder.build());
    }

    @VisibleForTesting
    void enqueueWork(@NonNull Intent intent) {
        ConnectIpmService.enqueueWork(context, intent);
    }

}
