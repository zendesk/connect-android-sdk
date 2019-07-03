package com.zendesk.connect;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.InstanceIdResult;
import com.zendesk.logger.Logger;

import com.zendesk.service.ErrorResponse;
import com.zendesk.service.RetrofitZendeskCallbackAdapter;
import com.zendesk.service.ZendeskCallback;
import com.zendesk.util.CollectionUtils;
import com.zendesk.util.StringUtils;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Default implementation of {@link ConnectClient}
 */
class DefaultConnectClient implements ConnectClient {

    private static final String LOG_TAG = "DefaultConnectClient";
    private static final String PUSH_CLIENT = "fcm";

    private StorageController storageController;
    private BaseQueue<User> userQueue;
    private BaseQueue<Event> eventQueue;
    private ConnectScheduler scheduler;
    private PushProvider pushProvider;
    private ConnectInstanceId instanceId;

    DefaultConnectClient(StorageController storageController,
                         BaseQueue<User> userQueue,
                         BaseQueue<Event> eventQueue,
                         ConnectScheduler scheduler,
                         PushProvider pushProvider,
                         ConnectInstanceId instanceId) {
        this.storageController = storageController;
        this.userQueue = userQueue;
        this.eventQueue = eventQueue;
        this.scheduler = scheduler;
        this.pushProvider = pushProvider;
        this.instanceId = instanceId;
    }

    /**
     * {@inheritDoc}
     * <p>
     *     If there is a currently active user and the user to be identified has a different
     *     user ID, then the user will be aliased with both IDs
     * </p>
     * @param user the user to be identified
     */
    @Override
    public void identifyUser(final User user) {
        if (user == null) {
            Logger.e(LOG_TAG, "Couldn't identify a null user");
            return;
        }

        User activeUser = storageController.getUser();
        final UserBuilder userBuilder = UserBuilder.newBuilder(user);

        // We alias the currently active user with the new user id if it is different
        if (activeUser != null && !activeUser.getUserId().equals(user.getUserId())) {
            userBuilder.setPreviousId(activeUser.getUserId());
        }

        OnSuccessListener<InstanceIdResult> successListener = new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                userBuilder.setFcmToken(instanceIdResult.getToken());
                persistUser(userBuilder.build());
            }
        };

        OnFailureListener failureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                persistUser(user);
            }
        };

        instanceId.getToken(successListener, failureListener);
    }

    /**
     * Stores the given user into storageController and queues the user for processing
     *
     * @param user the {@link User} to be stored and queued
     */
    private void persistUser(User user) {
        Logger.d(LOG_TAG, "Identifying user: %s", user.getUserId());
        storageController.saveUser(user);
        userQueue.add(user);
        scheduler.scheduleQueuedNetworkRequests();
    }

    /**
     * {@inheritDoc}
     * <p>
     *     The currently active user's id will be attached to the given {@link Event}. If there
     *     is no active user, then an anonymous user will be identified and attached to the event.
     * </p>
     */
    @Override
    public void trackEvent(Event event) {
        if (event == null) {
            Logger.e(LOG_TAG, "Couldn't track a null event");
            return;
        }

        User activeUser = storageController.getUser();

        // If there is no active user then we identify an anonymous user
        if (activeUser == null) {
            activeUser = UserBuilder.anonymousUser();
            identifyUser(activeUser);
        }
        Event eventToTrack = new Event(
                activeUser.getUserId(),
                event.getEvent(),
                event.getProperties(),
                event.getTimestamp());

        Logger.d(LOG_TAG, "Tracking event: %s", eventToTrack.toString());

        eventQueue.add(eventToTrack);
        scheduler.scheduleQueuedNetworkRequests();
    }

    @Override
    public void registerForPush() {
        OnSuccessListener<InstanceIdResult> successListener = new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                registerPushToken(instanceIdResult.getToken());
            }
        };

        OnFailureListener failureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Logger.e(LOG_TAG, "Couldn't register user for push", e);
            }
        };

        instanceId.getToken(successListener, failureListener);
    }

    /**
     * <p>
     *     Attaches the given device token to the active user and sends a register request
     * </p>
     *
     * @param token the device token
     */
    private void registerPushToken(final String token) {
        if (StringUtils.isEmpty(token)) {
            Logger.e(LOG_TAG, "There is no push token to register");
            return;
        }

        final User activeUser = storageController.getUser();

        if (activeUser == null) {
            Logger.d(LOG_TAG, "No active user, identifying anonymous user");
            UserBuilder userBuilder = UserBuilder.anonymousUserBuilder();
            userBuilder.setFcmToken(token);
            identifyUser(userBuilder.build());
            return;
        }

        PushRegistration registration = PushRegistrationFactory
                .create(activeUser.getUserId(), token);

        Callback<Void> callback = new RetrofitZendeskCallbackAdapter<>(new ZendeskCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Logger.d(LOG_TAG, "Successfully registered for push");
                UserBuilder userBuilder = UserBuilder.newBuilder(activeUser);
                userBuilder.setFcmToken(token);
                storageController.saveUser(userBuilder.build());
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                Logger.e(LOG_TAG, "Failed to register for push", errorResponse.getReason());
            }
        });

        sendRegisterRequest(registration, callback);
    }

    /**
     * Sends a register request to Connect to register the device for push
     *
     * @param registration an instance of {@link PushRegistration}
     * @param callback an implementation of {@link Callback} to handle the response
     */
    private void sendRegisterRequest(PushRegistration registration, Callback<Void> callback) {
        Call<Void> call = pushProvider.register(PUSH_CLIENT, registration);
        if (call == null) {
            Logger.e(LOG_TAG, "Couldn't send register for push request");
            return;
        }
        Logger.d(LOG_TAG, "Registering for push");
        call.enqueue(callback);
    }

    @Override
    public void disablePush() {
        final User activeUser = getUser();
        PushRegistration unregistration = createPushUnregistration(activeUser);

        if (unregistration != null) {
            Callback<Void> callback = new RetrofitZendeskCallbackAdapter<>(
                    new DisableRequestCallback<Void>());

            sendUnregisterRequest(unregistration, callback);
        }
    }

    /**
     * Checks if the given user has a push token
     *
     * @param user the {@link User} to be examined
     * @return true if the user has a push token, false otherwise
     */
    private boolean userHasPushToken(User user) {
        return user != null
                && CollectionUtils.isNotEmpty(user.getFcm())
                && !StringUtils.isEmpty(user.getFcm().get(0));
    }

    /**
     * Creates an instance of {@link PushRegistration} for disable push on this device
     *
     * @param user the user containing the device token
     * @return an instance of {@link PushRegistration}
     */
    private PushRegistration createPushUnregistration(User user) {
        if (!userHasPushToken(user)) {
            Logger.e(LOG_TAG, "There is no push token to disable");
            return null;
        }
        return new PushRegistration(user.getUserId(), user.getFcm().get(0));
    }

    /**
     * Sends an unregister call to Connect to disable the device push token
     *
     * @param unregistration an instance of {@link PushRegistration}
     * @param callback an implementation of {@link Callback} to handle the response
     */
    private void sendUnregisterRequest(PushRegistration unregistration, Callback<Void> callback) {
        Call<Void> call = pushProvider.unregister(PUSH_CLIENT, unregistration);
        if (call == null) {
            Logger.e(LOG_TAG, "Couldn't send disable push request");
            return;
        }
        Logger.d(LOG_TAG, "Disabling push");
        call.enqueue(callback);
    }

    @Override
    public void logoutUser() {
        Logger.d(LOG_TAG, "Logging out Connect user");

        disablePush();

        clearUserData();

        persistAnonymousUser(storageController, instanceId);
    }

    /**
     * Fetches the device push token and persists a new anonymous user with that token
     *
     * @param storageController an instance of {@link StorageController}
     * @param instanceId an instance of {@link ConnectInstanceId}
     */
    static void persistAnonymousUser(final StorageController storageController,
                                     final ConnectInstanceId instanceId) {
        final UserBuilder userBuilder = UserBuilder.anonymousUserBuilder();

        OnSuccessListener<InstanceIdResult> successListener = new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                userBuilder.setFcmToken(instanceIdResult.getToken());
                storageController.saveUser(userBuilder.build());
            }
        };

        OnFailureListener failureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                storageController.saveUser(userBuilder.build());
            }
        };

        instanceId.getToken(successListener, failureListener);
    }

    /**
     * Clears all data related to the current login
     */
    private void clearUserData() {
        storageController.clearUser();
        userQueue.clear();
        eventQueue.clear();
    }

    @Override
    public User getUser() {
        return storageController.getUser();
    }

    /**
     * An implementation of {@link ZendeskCallback} to be used as a basic callback for
     * disable push token requests.
     */
    static class DisableRequestCallback<T> extends ZendeskCallback<T> {
        @Override
        public void onSuccess(T t) {
            Logger.d(LOG_TAG, "Successfully disabled push");
        }

        @Override
        public void onError(ErrorResponse errorResponse) {
            Logger.e(LOG_TAG, "Failed to disable push", errorResponse.getReason());
        }
    }
}
