package com.zendesk.connect;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

/**
 * Class for interacting with {@link FirebaseInstanceId} to retrieve the device FCM token.
 * <p>
 *     Can be mocked for testing to return dummy tokens without interacting with Firebase
 * </p>
 */
class ConnectInstanceId {

    private FirebaseInstanceId firebaseInstanceId;

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
        firebaseInstanceId.getInstanceId()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
}
