package com.zendesk.connect;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.zendesk.logger.Logger;
import com.zendesk.util.StringUtils;

import javax.inject.Inject;

import static com.zendesk.connect.ConnectActionService.EXTRA_NOTIFICATION;

/**
 * Handles processing of {@link Intent} and metrics requests for the {@link ConnectActionService}
 * and for actions related to {@link IpmPayload}
 */
@ConnectScope
class ConnectActionProcessor {

    private static final String LOG_TAG = "ConnectActionProcessor";

    private final MetricRequestsProcessor metricRequestsProcessor;
    private final PackageManager packageManager;
    private final IntentBuilder intentBuilder;

    @Inject
    ConnectActionProcessor(MetricRequestsProcessor metricRequestsProcessor,
                           PackageManager packageManager,
                           IntentBuilder intentBuilder) {

        this.metricRequestsProcessor = metricRequestsProcessor;
        this.packageManager = packageManager;
        this.intentBuilder = intentBuilder;
    }

    /**
     * Determines which intent to open based on the payload received in the intent and the
     * configuration passed.
     *
     * @param payload the {@link SystemPushPayload} extracted from the received Intent
     * @param packageName the package name of the host app
     * @param openLaunchActivity whether or not we should open the host app launch activity
     * @param handleDeepLinks whether or not we should resolve the deeplink url
     * @return the intent to be opened, or null if any of the below is true:
     * * the package manager was null;
     * * the payload was null;
     * * openLaunchActivity and handleDeepLinks are both false;
     * * openLaunchActivity is false, handleDeepLinks is true, but {@link SystemPushPayload#getDeeplinkUrl()}
     *     couldn't be resolved to an Activity capable of handling it
     */
    @Nullable
    Intent resolveIntent(SystemPushPayload payload,
                         String packageName,
                         boolean openLaunchActivity,
                         boolean handleDeepLinks) {

        if (packageManager == null) {
            Logger.e(LOG_TAG, "Package manager was null, unable to resolve intent");
            return null;
        }

        if (payload == null) {
            Logger.e(LOG_TAG, "Payload was null, unable to resolve intent");
            return null;
        }

        Intent intentToOpen = null;
        Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
        if (openLaunchActivity && launchIntent != null) {
            intentToOpen = intentBuilder.from(launchIntent)
                    .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .build();
        }

        if (handleDeepLinks && payload.getDeeplinkUrl() != null) {
            Intent deepLinkIntent = resolveDeepLinkIntent(payload.getDeeplinkUrl());
            if (deepLinkIntent != null) {
                intentToOpen = deepLinkIntent;
            }
        }

        Logger.d(LOG_TAG, "Intent resolved to %s", intentToOpen);
        return intentToOpen;
    }

    /**
     * Attempts to extract the extra from the given {@link Intent} and deserialise it into a
     * {@link SystemPushPayload}.
     *
     * @param intent the received {@link Intent}
     * @param packageName the host app package name
     * @return an instance of {@link SystemPushPayload}, or null if an error was encountered
     */
    @Nullable
    SystemPushPayload extractPayload(Intent intent, String packageName) {
        String expectedActionName = packageName + ConnectActionService.ACTION_OPEN_NOTIFICATION;
        boolean isValidIntent = verifyIntent(intent, expectedActionName);
        if (!isValidIntent) {
            Logger.e(LOG_TAG, "Intent was null or contained invalid action name");
            return null;
        }

        SystemPushPayload extractedPayload = extractPayloadFromIntent(intent);

        if (extractedPayload != null) {
            metricRequestsProcessor.sendOpenedRequest(extractedPayload.getInstanceId(),
                    extractedPayload.isTestPush());
        }

        return extractedPayload;
    }

    /**
     * Attempts to extract a {@link SystemPushPayload} from the given {@link Intent}
     *
     * @param intent the received {@link Intent}
     * @return an instance of {@link SystemPushPayload} or null
     */
    @Nullable
    @VisibleForTesting
    SystemPushPayload extractPayloadFromIntent(Intent intent) {
        Parcelable extra = intent.getParcelableExtra(EXTRA_NOTIFICATION);
        return extra instanceof SystemPushPayload ? (SystemPushPayload) extra : null;
    }

    /**
     * Verifies that the given intent is not null and contains the expected action
     *
     * @param intent the {@link Intent} to verify
     * @param expectedActionName the expected action
     * @return true if the action is valid, false otherwise
     */
    @VisibleForTesting
    boolean verifyIntent(Intent intent, String expectedActionName) {
        if (intent == null) {
            return false;
        }
        String action = intent.getAction();
        return !StringUtils.isEmpty(action) && action.equals(expectedActionName);
    }

    /**
     * Resolves and builds an {@link Intent} from the provided deep link URL.
     * It also tries to resolve an activity for the built Intent.
     *
     * @param deepLinkUrl the deep link url
     * @return an {@link Intent} to launch the deep link, or null if the URL was invalid or if no
     * activity is capable of handling the intent
     */
    @Nullable
    Intent resolveDeepLinkIntent(String deepLinkUrl) {
        if (StringUtils.isEmpty(deepLinkUrl)) {
            Logger.w(LOG_TAG, "Deep link url was null or empty");
            return null;
        }

        Intent intent = intentBuilder
                .withAction(Intent.ACTION_VIEW)
                .withData(deepLinkUrl)
                .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .build();

        if (intent.resolveActivity(packageManager) != null) {
            return intent;
        } else {
            Logger.w(LOG_TAG, "Intent for url %s couldn't be resolved to any Activity", deepLinkUrl);
            return null;
        }
    }

}
