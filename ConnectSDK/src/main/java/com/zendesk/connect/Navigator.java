package com.zendesk.connect;

import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

/**
 * Class used for navigating to activities
 */
@ConnectScope
class Navigator {

    private IntentBuilder intentBuilder;

    /**
     * Creates an instance of {@link Navigator}
     *
     * @param intentBuilder an instance of {@link IntentBuilder}
     */
    @Inject
    Navigator(IntentBuilder intentBuilder) {
        this.intentBuilder = intentBuilder;
    }

    /**
     * Starts the {@link IpmActivity}
     *
     * @param context an instance of {@link Context}
     */
    void startIpmActivity(Context context) {
        intentBuilder.withClassName(context, IpmActivity.class.getName());
        intentBuilder.withFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intentBuilder.build());
    }

}
