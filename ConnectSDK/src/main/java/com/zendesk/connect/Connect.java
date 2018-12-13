package com.zendesk.connect;

import android.app.Application;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.zendesk.logger.Logger;

/**
 * Connect SDK public entry point. Use this object to initialise the Connect SDK.
 */
public enum Connect {
    INSTANCE;

    private static final String LOG_TAG = "Connect";
    private static final String NOT_ENABLED_LOG = "Connect SDK is not enabled";
    private static final String NOT_INITIALIZED_LOG = "Connect SDK has not been initialised";

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
                .connectModule(new ConnectModule(application.getApplicationContext()))
                .connectStorageModule(new ConnectStorageModule())
                .connectNetworkModule(new ConnectNetworkModule(privateKey))
                .connectNotificationModule(new ConnectNotificationModule())
                .build();

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
    void init(ConnectComponent component) {
        boolean coldStart = connectComponent == null;
        connectComponent = component;

        // If this is init call is from a cold start of the app then we fetch config
        // and schedule a config job to repeat every hour.
        if (coldStart) {
            component.scheduler().scheduleSingleConfigRequest();
            component.scheduler().scheduleRecurringConfigRequests();
        }

        // If there is no stored user then we store an anonymous user
        final StorageController storageController = component.storageController();
        if (storageController.getUser() == null) {
            DefaultConnectClient.persistAnonymousUser(component.storageController(), component.instanceId());
        }
    }

    /**
     * <p>
     *     Allows us to reset the Connect instance for testing
     * </p>
     */
    @VisibleForTesting
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
        return connectComponent != null;
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
    boolean isEnabled() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, NOT_INITIALIZED_LOG);
            return false;
        }
        Config config = connectComponent.storageController().getConfig();
        return config == null || config.isEnabled();
    }

    /**
     * <p>
     *     Provides a concrete implementation of {@link BaseStorage} for storing things like
     *     {@link User} and {@link Config}.
     * </p>
     *
     * @return an implementation of {@link BaseStorage}, or {@code null} if Connect
     *          wasn't initialised.
     */
    @Nullable
    StorageController storageController() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, NOT_INITIALIZED_LOG);
            return null;
        }
        return connectComponent.storageController();
    }

    /**
     * <p>
     *     Gets an instance of {@link ConfigProvider}
     * </p>
     *
     * @return an instance of {@link ConfigProvider}
     */
    ConfigProvider configProvider() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, NOT_INITIALIZED_LOG);
            return null;
        }
        return connectComponent.configProvider();
    }

    /**
     * <p>
     *     Gets an instance of {@link IdentifyProvider}
     * </p>
     *
     * @return an instance of {@link IdentifyProvider}
     */
    IdentifyProvider identifyProvider() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, NOT_INITIALIZED_LOG);
            return null;
        }
        return connectComponent.identifyProvider();
    }

    /**
     * <p>
     *     Gets an instance of {@link EventProvider}
     * </p>
     *
     * @return an instance of {@link EventProvider}
     */
    EventProvider eventProvider() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, NOT_INITIALIZED_LOG);
            return null;
        }
        return connectComponent.eventProvider();
    }

    /**
     * <p>
     *     Gets an instance of {@link PushProvider}
     * </p>
     *
     * @return an instance of {@link PushProvider}
     */
    PushProvider pushProvider() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, NOT_INITIALIZED_LOG);
            return null;
        }
        return connectComponent.pushProvider();
    }

    MetricsProvider metricsProvider() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, NOT_INITIALIZED_LOG);
            return null;
        }
        return connectComponent.metricsProvider();
    }

    TestSendProvider testSendProvider() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, NOT_INITIALIZED_LOG);
            return null;
        }
        return connectComponent.testSendProvider();
    }

    /**
     * <p>
     *     Gets the {@link BaseQueue} used for queuing {@link User} objects
     * </p>
     *
     * @return an implementation of {@link BaseQueue}, or {@code null} if Connect
     *          wasn't initialised.
     */
    BaseQueue<User> userQueue() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, NOT_INITIALIZED_LOG);
            return null;
        }
        return connectComponent.userQueue();
    }

    /**
     * <p>
     *     Gets the {@link BaseQueue} used for queuing {@link Event} objects
     * </p>
     *
     * @return an implementation of {@link BaseQueue}, or {@code null} if Connect
     *          wasn't initialised.
     */
    BaseQueue<Event> eventQueue() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, NOT_INITIALIZED_LOG);
            return null;
        }
        return connectComponent.eventQueue();
    }

    /**
     * <p>
     *     Gets the {@link NotificationProcessor} used for parsing incoming push notifications
     *     and constructing {@link android.app.Notification} objects to display
     * </p>
     *
     * @return an instance of {@link NotificationProcessor}, or {@code null} if Connect
     *          wasn't initialised.
     */
    NotificationProcessor notificationProcessor() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, NOT_INITIALIZED_LOG);
            return null;
        }
        return connectComponent.notificationProcessor();
    }

    /**
     * <p>
     *     Gets the {@link ConnectActionProcessor} used for processing incoming intents received
     *     by the {@link ConnectActionService}
     * </p>
     *
     * @return an instance of {@link ConnectActionProcessor}, or {@code null} if Connect
     *          wasn't initialised
     */
    ConnectActionProcessor actionProcessor() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, NOT_INITIALIZED_LOG);
            return null;
        }
        return connectComponent.actionProcessor();
    }

    /**
     * <p>
     *     Gets the {@link MetricRequestsProcessor} used for sending metrics requests
     *     when push notifications are received
     * </p>
     *
     * @return an instance of {@link MetricRequestsProcessor}, or {@code null} if Connect
     *          wasn't initialised
     */
    MetricRequestsProcessor metricsProcessor() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, NOT_INITIALIZED_LOG);
            return null;
        }
        return connectComponent.metricsProcessor();
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
        if (!isEnabled()) {
            Logger.e(LOG_TAG, NOT_ENABLED_LOG);
            return;
        }
        connectComponent.client().identifyUser(user);
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
        if (!isEnabled()) {
            Logger.e(LOG_TAG, NOT_ENABLED_LOG);
            return;
        }
        connectComponent.client().trackEvent(event);
    }

    /**
     * <p>
     *     Registers the currently active user for push notifications.
     * </p>
     */
    public void registerForPush() {
        if (!isEnabled()) {
            Logger.e(LOG_TAG, NOT_ENABLED_LOG);
            return;
        }
        connectComponent.client().registerForPush();
    }

    /**
     * <p>
     *     Disables push notifications for the currently active user.
     * </p>
     */
    public void disablePush() {
        if (!isEnabled()) {
            Logger.e(LOG_TAG, NOT_ENABLED_LOG);
            return;
        }
        connectComponent.client().disablePush();
    }

    /**
     * <p>
     *     Logs the currently active user out of the SDK by clearing their identity
     *     from local storage and disabling push notifications.
     * </p>
     */
    public void logoutUser() {
        if (!isEnabled()) {
            Logger.e(LOG_TAG, NOT_ENABLED_LOG);
            return;
        }
        connectComponent.client().logoutUser();
    }

    /**
     * <p>
     *     Gets the currently active user
     * </p>
     * @return the currently active {@link User}, or {@code null} if the user does
     *          not exist or Connect has not been initialised
     */
    public User getUser() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, NOT_ENABLED_LOG);
            return null;
        }
        return connectComponent.client().getUser();
    }
}
