package com.zendesk.connect;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.InstanceIdResult;
import com.zendesk.logger.Logger;

/**
 * Connect SDK public entry point. Use this object to initialise the Connect SDK.
 */
public enum Connect {
    INSTANCE;

    private static final String LOG_TAG = "Connect";

    private ConnectComponent connectComponent;

    /**
     * <p>Initialise Connect SDK</p>
     *
     * @param context an application context
     */
    public void init(Context context, String apiKey) {
        ConnectComponent connectComponent = DaggerConnectComponent.builder()
                .connectModule(new ConnectModule(context))
                .connectStorageModule(new ConnectStorageModule(context))
                .connectNetworkModule(new ConnectNetworkModule(apiKey))
                .build();

        init(connectComponent);
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
            final User user = UserBuilder.anonymousUser();
            OnSuccessListener<InstanceIdResult> successListener = new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    user.setFcmToken(instanceIdResult.getToken());
                    storageController.saveUser(user);
                }
            };

            OnFailureListener failureListener = new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    storageController.saveUser(user);
                }
            };

            component.instanceId().getToken(successListener, failureListener);
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
    public boolean isEnabled() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, "Connect SDK has not been initialised");
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
     * <p>
     *     Temporarily exposing storage for the Outbound SDK to use. Will be restricted
     *     once the Outbound SDK is removed from the project.
     * </p>
     *
     * @return an implementation of {@link BaseStorage}, or {@code null} if Connect
     *          wasn't initialised.
     */
    @Nullable
    StorageController storageController() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, "Connect SDK has not been initialised");
            return null;
        }
        return connectComponent.storageController();
    }

    /**
     * Temporarily exposing a queue for the Outbound SDK to use. Will be restricted
     * once the Outbound SDK is removed from the project.
     */
    @Nullable
    public BaseQueue<String> outboundQueue() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, "Connect SDK has not been initialised");
            return null;
        }
        return connectComponent.outboundQueue();
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
            Logger.e(LOG_TAG, "Connect SDK has not been initialised");
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
            Logger.e(LOG_TAG, "Connect SDK has not been initialised");
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
            Logger.e(LOG_TAG, "Connect SDK has not been initialised");
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
            Logger.e(LOG_TAG, "Connect SDK has not been initialised");
            return null;
        }
        return connectComponent.pushProvider();
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
            Logger.e(LOG_TAG, "Connect SDK has not been initialised");
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
            Logger.e(LOG_TAG, "Connect SDK has not been initialised");
            return null;
        }
        return connectComponent.eventQueue();
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
            Logger.e(LOG_TAG, "Connect SDK is not enabled");
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
            Logger.e(LOG_TAG, "Connect SDK is not enabled");
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
            Logger.e(LOG_TAG, "Connect SDK is not enabled");
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
            Logger.e(LOG_TAG, "Connect SDK is not enabled");
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
    public void logout() {
        if (!isEnabled()) {
            Logger.e(LOG_TAG, "Connect SDK is not enabled");
            return;
        }
        connectComponent.client().logout();
    }

    /**
     * <p>
     *     Gets the currently active user
     * </p>
     * @return the currently active {@link User}, or {@code null} if the user does
     *          not exist or Connect has not been initialised
     */
    public User getActiveUser() {
        if (!isInitialised()) {
            Logger.e(LOG_TAG, "Connect SDK has not been initialised");
            return null;
        }
        return connectComponent.client().getActiveUser();
    }
}
