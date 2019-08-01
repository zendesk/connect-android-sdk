package com.zendesk.connect;

import android.content.Context;

import javax.inject.Inject;

/**
 * Simple factory for creating instances of {@link IpmPushStrategyFactory}
 */
@ConnectScope
class IpmPushStrategyFactory {

    private final IpmPayloadParser payloadParser;
    private final Context context;
    private final IntentBuilder intentBuilder;

    /**
     * Creates an instance of {@link IpmPushStrategyFactory}
     *
     * @param payloadParser an instance of {@link IpmPayloadParser}
     */
    @Inject
    IpmPushStrategyFactory(IpmPayloadParser payloadParser,
                           Context context,
                           IntentBuilder intentBuilder) {
        this.payloadParser = payloadParser;
        this.context = context;
        this.intentBuilder = intentBuilder;
    }

    /**
     * Creates an instance of {@link IpmPushStrategy}
     *
     * @return an instance of {@link IpmPushStrategy}
     */
    PushStrategy create() {
        return new IpmPushStrategy(payloadParser, context, intentBuilder);
    }

}
