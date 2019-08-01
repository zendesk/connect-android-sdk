package com.zendesk.connect;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

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
 *     Integrators can extend this class and override {@link #onOpenNotification(SystemPushPayload)}
 *     to handle routing of notifications themselves. Data from the notification payload will be
 *     available, including any deep link URLs that may have been ignored.
 * </p>
 */
public class ConnectActionService extends IntentService {

    private static final String LOG_TAG = "ConnectActionService";

    static final String ACTION_OPEN_NOTIFICATION = ".connect.action.OPEN_NOTIFICATION";
    static final String EXTRA_NOTIFICATION = BuildConfig.APPLICATION_ID + ".extra.NOTIFICATION";

    @VisibleForTesting
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    static boolean openLaunchActivity = true;

    @VisibleForTesting
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    static boolean handleDeepLinks = true;

    public ConnectActionService() {
        super(LOG_TAG);
    }

    @Override
    protected final void onHandleIntent(@Nullable Intent intent) {
        ConnectComponent connectComponent = Connect.INSTANCE.getComponent();
        if (connectComponent == null) {
            Logger.e(LOG_TAG, "Connect has not been initialised, ending service");
            return;
        }

        String packageName = getPackageName();
        ConnectActionProcessor actionProcessor = connectComponent.actionProcessor();
        SystemPushPayload payload = actionProcessor.extractPayload(intent, packageName);

        if (payload == null) {
            Logger.e(LOG_TAG, "Payload couldn't be extracted from the intent, ending service");
            return;
        }

        Intent intentToOpen = actionProcessor.resolveIntent(
                payload,
                packageName,
                openLaunchActivity,
                handleDeepLinks);

        if (intentToOpen != null) {
            startActivity(intentToOpen);
        } else {
            Logger.e(LOG_TAG, "Intent was null, unable to open activity");
        }

        onOpenNotification(payload);
    }

    /**
     * This method will be called after a Connect notification has been opened. Override this method
     * to provide your own logic for routing after a notification has been opened.
     *
     * @param payload an instance of {@link SystemPushPayload}
     */
    public void onOpenNotification(SystemPushPayload payload) {
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
