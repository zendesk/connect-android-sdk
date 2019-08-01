package com.zendesk.connect;

import android.app.Application;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import com.zendesk.logger.Logger;

/**
 * Connect SDK public entry point. Use this object to initialise the Connect SDK.
 */
public enum Connect {
    INSTANCE;

    private static final String LOG_TAG = "Connect";
    private static final String NOT_ENABLED_LOG = "Connect SDK is not enabled";
    private static final String NOT_INITIALIZED_LOG = "Connect SDK has not been initialised";

    private static final String CONNECT_BASE_URL = "https://api.outbound.io";

    static final String CLIENT_VERSION = BuildConfig.VERSION_NAME;
    static final String CLIENT_PLATFORM = "android";

    private ConnectComponent connectComponent;

    /**
     * <p>Initialise Connect SDK</p>
     *
     * @param application an instance of {@link Application}
     * @param privateKey a Connect private API key
     */
    public void init(Application application, String privateKey) {
        ConnectComponent connectComponent = DaggerConnectComponent.builder()
                .application(application)
                .connectApiConfiguration(new ConnectApiConfiguration(CONNECT_BASE_URL, privateKey))
                .build();

        updateStoredPrivateKey(connectComponent, privateKey);

        init(connectComponent);

        TouchGestureMonitor.add(application);
    }

    /**
     * <p>
     *     Internal init method for injecting {@link ConnectComponent}. Component can be
     *     swapped for test dependencies if needed.
     * </p>
     *
     * @param component: Dagger component used to initialise the SDK
     */
    @VisibleForTesting
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    void init(ConnectComponent component) {
        boolean coldStart = connectComponent == null;
        connectComponent = component;

        // If this is init call is from a cold start of the app then we fetch config
        // and schedule a config job to repeat every hour.
        if (coldStart) {
            component.scheduler().scheduleRecurringConfigRequests();
        }

        // If there is no stored user then we store an anonymous user
        final StorageController storageController = component.storageController();
        if (storageController.getUser() == null) {
            DefaultConnectClient.persistAnonymousUser(component.storageController(), component.instanceId());
        }

        // Start listening for Lifecycle events
        connectComponent.foregroundListener();
    }

    /**
     * <p>
     *     Internal method to retrieve the instance of {@link ConnectComponent} to enable access
     *     to the sub-components factory methods and other exposed dependencies.
     * </p>
     *
     * @return an instance of {@link ConnectComponent} if initialised, null otherwise
     */
    @Nullable
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    ConnectComponent getComponent() {
        return isInitialised() ? connectComponent : null;
    }

    /**
     * Clears all storage if the provided private key doesn't match the one stored on device
     *
     * @param connectComponent an instance of {@link ConnectComponent}
     * @param providedPrivateKey the key provided by the host app
     */
    @VisibleForTesting
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    void updateStoredPrivateKey(ConnectComponent connectComponent, String providedPrivateKey) {
        StorageController storageController = connectComponent.storageController();

        if (storageController.isNewPrivateKey(providedPrivateKey)) {
            storageController.clearAllStorage();
            connectComponent.userQueue().clear();
            connectComponent.eventQueue().clear();
        }

        storageController.savePrivateKey(providedPrivateKey);
    }

    /**
     * <p>
     *     Allows us to reset the Connect instance for testing
     * </p>
     */
    @VisibleForTesting
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    void reset() {
        connectComponent = null;
    }

    /**
     * <p>
     *     Gets whether or not the Connect SDK has been initialised correctly.
     * </p>
     *
     * @return true if Connect has been initialised, false otherwise.
     */
    boolean isInitialised() {
        if (connectComponent == null) {
            Logger.e(LOG_TAG, NOT_INITIALIZED_LOG);
            return false;
        }
        return true;
    }

    /**
     * <p>
     *     Gets whether or not the Connect SDK is allowed to make network requests. Will also return
     *     false if the Connect SDK has not been initialised. If no config has been stored yet then
     *     we assume we are enabled until a config requests confirms otherwise.
     * </p>
     *
     * @return true if network requests are enabled, false otherwise.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    boolean isEnabled() {
        if (!isInitialised()) {
            return false;
        }

        Config config = connectComponent.storageController().getConfig();
        if (config != null && !config.isEnabled()) {
            Logger.e(LOG_TAG, NOT_ENABLED_LOG);
            return false;
        }

        return true;
    }

    /**
     * <p>
     *     Identify a Connect user. Use {@link UserBuilder} to construct a
     *     {@link User} object with all of the details required. For an anonymous user you
     *     can use {@link UserBuilder#anonymousUser()}.
     * </p>
     * <p>
     *     Identify should be called when a user needs to be remembered, or the existing
     *     information for a user needs to be updated.
     * </p>
     *
     * @param user the {@link User} to be identified
     */
    public void identifyUser(User user) {
        if (isEnabled()) {
            connectComponent.client().identifyUser(user);
        }
    }

    /**
     * <p>
     *     Track a Connect event. Use {@link EventFactory} to quickly create {@link Event}
     *     objects.
     * </p>
     * <p>
     *     Track should be called whenever something important happens that you want to
     *     monitor in your Connect dashboard.
     * </p>
     *
     * @param event the {@link Event} to be tracked
     */
    public void trackEvent(Event event) {
        if (isEnabled()) {
            connectComponent.client().trackEvent(event);
        }
    }

    /**
     * <p>
     *     Registers the currently active user for push notifications.
     * </p>
     */
    public void registerForPush() {
        if (isEnabled()) {
            connectComponent.client().registerForPush();
        }
    }

    /**
     * <p>
     *     Disables push notifications for the currently active user.
     * </p>
     */
    public void disablePush() {
        if (isEnabled()) {
            connectComponent.client().disablePush();
        }
    }

    /**
     * <p>
     *     Logs the currently active user out of the SDK by clearing their identity
     *     from local storage and disabling push notifications.
     * </p>
     */
    public void logoutUser() {
        if (isEnabled()) {
            connectComponent.client().logoutUser();
        }
    }

    /**
     * <p>
     *     Gets the currently active user
     * </p>
     * @return the currently active {@link User}, or {@code null} if the user does
     *          not exist or Connect has not been initialised
     */
    @Nullable
    public User getUser() {
        return isInitialised() ? connectComponent.client().getUser() : null;
    }

}
