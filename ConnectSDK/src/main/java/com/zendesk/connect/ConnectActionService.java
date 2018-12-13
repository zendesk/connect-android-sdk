package com.zendesk.connect;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.zendesk.logger.Logger;

/**
 * <p>
 *     Handles tasks related to notifications sent by Connect. {@link ConnectActionService} or a
 *     subclass should be registered in the Android manifest of the host app.
 * </p>
 *
 * <pre>{@code
 *     <service android:name="com.zendesk.connect.ConnectActionService">
 *         <intent-filter>
 *             <action android:name="{packageName}.connect.action.OPEN_NOTIFICATION"/>
 *         </intent-filter>
 *     </service>
 * }</pre>
 *
 * <p>
 *     The default behaviour of the SDK is to handle deep links if they are present in the
 *     notification payload, or to open the host app's launch activity if there is no deep link.
 *     The following static fields can be updated to change the behaviour:
 * </p>
 *
 * <ul>
 *     <li>
 *         {@link #handleDeepLinks} is {@code true} by default. If set to false, then the SDK
 *         will ignore any deep links included in the notification payload.
 *     </li>
 *     <li>
 *         {@link #openLaunchActivity} is {@code true} by default. If set to false,
 *         then the SDK will not attempt to launch the host app's launch activity as a fallback.
 *     </li>
 *     <li>
 *         If both are updated to false then the SDK will not attempt to launch any intent
 *     </li>
 * </ul>
 *
 * <p>
 *     Integrators can extend this class and override {@link #onOpenNotification(NotificationPayload)}
 *     to handle routing of notifications themselves. Data from the notification payload will be
 *     available, including any deep link URLs that may have been ignored.
 * </p>
 */
public class ConnectActionService extends IntentService {

    private static final String LOG_TAG = "ConnectActionService";

    static final String ACTION_OPEN_NOTIFICATION = ".connect.action.OPEN_NOTIFICATION";
    static final String EXTRA_NOTIFICATION = BuildConfig.APPLICATION_ID + ".extra.NOTIFICATION";

    private static boolean openLaunchActivity = true;
    private static boolean handleDeepLinks = true;

    public ConnectActionService() {
        super(LOG_TAG);
    }

    @Override
    protected final void onHandleIntent(@Nullable Intent intent) {
        if (!Connect.INSTANCE.isInitialised()) {
            Logger.d(LOG_TAG, "Connect has not been initialised, ending service");
            return;
        }

        String packageName = getPackageName();
        ConnectActionProcessor actionProcessor = Connect.INSTANCE.actionProcessor();

        NotificationPayload payload = extractPayload(intent, packageName, actionProcessor);
        if (payload == null) {
            Logger.e(LOG_TAG, "Payload couldn't extracted from the intent");
            return;
        }

        MetricRequestsProcessor metricsProcessor = Connect.INSTANCE.metricsProcessor();
        sendMetrics(payload, metricsProcessor);

        Intent intentToOpen = resolveIntent(payload, packageName);
        if (intentToOpen != null) {
            startActivity(intentToOpen);
        } else {
            Logger.e(LOG_TAG, "Intent was null, unable to open activity");
        }

        onOpenNotification(payload);
    }

    /**
     * Attempts to extract the extra from the given {@link Intent} and deserialise it into a
     * {@link NotificationPayload}.
     *
     * @param intent the received {@link Intent}
     * @param packageName the host app package name
     * @param actionProcessor an instance of {@link ConnectActionProcessor}
     * @return an instance of {@link NotificationPayload}, or null if an error was encountered
     */
    private NotificationPayload extractPayload(Intent intent, String packageName, ConnectActionProcessor actionProcessor) {
        if (actionProcessor == null) {
            Logger.e(LOG_TAG, "Action processor is null");
            return null;
        }

        String expectedActionName = packageName + ACTION_OPEN_NOTIFICATION;
        boolean isValidIntent = actionProcessor.verifyIntent(intent, expectedActionName);
        if (!isValidIntent) {
            Logger.e(LOG_TAG, "Intent was null or contained invalid action name");
            return null;
        }

        return actionProcessor.extractPayloadFromIntent(intent);
    }

    /**
     * Sends any necessary metrics for this action
     *
     * @param payload the {@link NotificationPayload} deserialised from the intent extra
     * @param metricsProcessor an instance of {@link MetricRequestsProcessor} for handling metrics requests
     */
    private void sendMetrics(NotificationPayload payload, MetricRequestsProcessor metricsProcessor) {
        if (metricsProcessor != null) {
            metricsProcessor.sendOpenedRequest(payload);
        } else {
            Logger.d(LOG_TAG, "Metrics processor was null, unable to send metrics");
        }
    }

    /**
     * Determines which intent to open based on the service configuration and the
     * payload received in the intent.
     *
     * @param payload the {@link NotificationPayload} extracted from the received Intent
     * @param packageName the package name of the host app
     * @return the intent to be opened
     */
    private Intent resolveIntent(NotificationPayload payload, String packageName) {

        Uri deepLink = Uri.parse(payload.getDeeplinkUrl());
        PackageManager packageManager = getApplicationContext().getPackageManager();

        Intent intentToOpen = openLaunchActivity
                ? packageManager.getLaunchIntentForPackage(packageName)
                : null;

        if (deepLink != null && handleDeepLinks) {
            Intent deepLinkIntent = new Intent(Intent.ACTION_VIEW);
            deepLinkIntent.setData(deepLink);
            deepLinkIntent.setPackage(packageName);

            ComponentName componentName = deepLinkIntent.resolveActivity(packageManager);
            if (componentName != null) {
                intentToOpen = deepLinkIntent;
            }
        }

        if (intentToOpen != null) {
            intentToOpen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        return intentToOpen;
    }

    /**
     * This method will be called after a Connect notification has been opened. Override this method
     * to provide your own logic for routing after a notification has been opened.
     *
     * @param payload an instance of {@link NotificationPayload}
     */
    public void onOpenNotification(NotificationPayload payload) {
        Logger.d(LOG_TAG, "onOpenNotification has not been implemented by the integrator");
    }

    /**
     * Modifies the behaviour of this service to open the host app launch activity when a
     * notification is opened and there is no usable deep link. This is {@code true} by default.
     *
     * @param shouldOpenLaunchActivity true if the launch activity should be launched as a fallback,
     *                                  false otherwise.
     */
    public static void shouldOpenLaunchActivityByDefault(boolean shouldOpenLaunchActivity) {
        openLaunchActivity = shouldOpenLaunchActivity;
    }

    /**
     * Modifies the behaviour of the service to open any deep links when a notification is
     * opened if any dependencies in the host app package can handle it. This is {@code true}
     * by default.
     *
     * @param shouldHandleDeepLinks true if deep links should be handled,
     *                              false otherwise.
     */
    public static void shouldHandleDeepLinks(boolean shouldHandleDeepLinks) {
        handleDeepLinks = shouldHandleDeepLinks;
    }
}
