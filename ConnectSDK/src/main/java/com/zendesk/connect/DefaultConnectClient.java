package com.zendesk.connect;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.InstanceIdResult;
import com.zendesk.logger.Logger;

import com.zendesk.service.ErrorResponse;
import com.zendesk.service.RetrofitZendeskCallbackAdapter;
import com.zendesk.service.ZendeskCallback;
import retrofit2.Call;

/**
 * Default implementation of {@link ConnectClient}
 */
class DefaultConnectClient implements ConnectClient {

    private static final String LOG_TAG = "DefaultConnectClient";

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
            Logger.d(LOG_TAG, "Couldn't identify a null user");
            return;
        }

        User activeUser = storageController.getUser();

        // We alias the currently active user with the new user id if it is different
        if (activeUser != null && !activeUser.getUserId().equals(user.getUserId())) {
            user.setPreviousId(activeUser.getUserId());
        }

        OnSuccessListener<InstanceIdResult> successListener = new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                user.setFcmToken(instanceIdResult.getToken());
                persistUser(user);
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
            Logger.d(LOG_TAG, "Couldn't track a null event");
            return;
        }

        User activeUser = storageController.getUser();

        // If there is no active user then we identify an anonymous user
        if (activeUser == null) {
            activeUser = UserBuilder.anonymousUser();
            identifyUser(activeUser);
        }
        event.setUserId(activeUser.getUserId());

        Logger.d(LOG_TAG, "Tracking event: %s", event.toString());

        eventQueue.add(event);
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
        if (TextUtils.isEmpty(token)) {
            Logger.d(LOG_TAG, "No token to register");
            return;
        }

        final User activeUser = storageController.getUser();

        if (activeUser == null) {
            Logger.d(LOG_TAG, "No active user, identifying anonymous user");
            User anonymousUser = UserBuilder.anonymousUser();
            anonymousUser.setFcmToken(token);
            identifyUser(anonymousUser);
            return;
        }

        PushRegistration registration = PushRegistrationFactory
                .create(activeUser.getUserId(), token);

        Call<Void> call = pushProvider.register(registration);
        if (call == null) {
            Logger.d(LOG_TAG, "Couldn't send register for push request");
            return;
        }

        Logger.d(LOG_TAG, "Registering for push");
        call.enqueue(new RetrofitZendeskCallbackAdapter<Void, Void>(new ZendeskCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Logger.d(LOG_TAG, "Successfully registered for push");
                activeUser.setFcmToken(token);
                storageController.saveUser(activeUser);
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                Logger.e(LOG_TAG, "Failed to register for push", errorResponse.getReason());
            }
        }));
    }

    @Override
    public void disablePush() {
        final User activeUser = storageController.getUser();
        if (activeUser == null || TextUtils.isEmpty(activeUser.getFcmToken())) {
            Logger.d(LOG_TAG, "There is no push token to disable");
            return;
        }

        PushRegistration unregistration = PushRegistrationFactory
                .create(activeUser.getUserId(), activeUser.getFcmToken());

        Call<Void> call = pushProvider.unregister(unregistration);
        if (call == null) {
            Logger.d(LOG_TAG, "Couldn't send disable push request");
            return;
        }

        Logger.d(LOG_TAG, "Disabling push");
        call.enqueue(new RetrofitZendeskCallbackAdapter<Void, Void>(new ZendeskCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Logger.d(LOG_TAG, "Successfully disabled push");
                activeUser.setFcmToken(null);
                storageController.saveUser(activeUser);
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                Logger.e(LOG_TAG, "Failed to disable push", errorResponse.getReason());
            }
        }));
    }

    @Override
    public void logout() {
        Logger.d(LOG_TAG, "Logging out Connect user");

        disablePush();

        storageController.clearUser();

        final User anonymousUser = UserBuilder.anonymousUser();

        OnSuccessListener<InstanceIdResult> successListener = new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                anonymousUser.setFcmToken(instanceIdResult.getToken());
                storageController.saveUser(anonymousUser);
            }
        };

        OnFailureListener failureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                storageController.saveUser(anonymousUser);
            }
        };

        instanceId.getToken(successListener, failureListener);
    }

    @Override
    public User getActiveUser() {
        return storageController.getUser();
    }
}
