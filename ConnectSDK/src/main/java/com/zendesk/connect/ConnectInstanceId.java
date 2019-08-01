package com.zendesk.connect;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.zendesk.logger.Logger;

import javax.inject.Inject;

/**
 * Class for interacting with {@link FirebaseInstanceId} to retrieve the device FCM token.
 * <p>
 *     Can be mocked for testing to return dummy tokens without interacting with Firebase
 * </p>
 */
@ConnectScope
class ConnectInstanceId {

    private static final String LOG_TAG = "ConnectInstanceId";

    private FirebaseInstanceId firebaseInstanceId;

    @Inject
    ConnectInstanceId(FirebaseInstanceId firebaseInstanceId) {
        this.firebaseInstanceId = firebaseInstanceId;
    }

    /**
     * Attempts to retrieve the device token from {@link FirebaseInstanceId}
     *
     * @param successListener the {@link OnSuccessListener} to be called when the token is
     *                        successfully retrieved.
     * @param failureListener the {@link OnFailureListener} to be called when the token is
     *                        not successfully retrieved.
     */
    void getToken(OnSuccessListener<InstanceIdResult> successListener, OnFailureListener failureListener) {
        if (successListener == null || failureListener == null) {
            Logger.e(LOG_TAG, "Success listener and failure listener must be non null");
            return;
        }

        firebaseInstanceId.getInstanceId()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
}
