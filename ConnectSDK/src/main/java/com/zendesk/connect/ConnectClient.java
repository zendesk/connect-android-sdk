package com.zendesk.connect;

import android.util.Log;

import javax.inject.Inject;

/**
 * Standard implementation of {@link Client}
 */
public class ConnectClient implements Client {

    private final String LOG_TAG = "ConnectClient";

    @Inject
    ConnectClient() {

    }

    @Override
    public void identifyUser(User user) {
        Log.d(LOG_TAG, "Not yet implemented");
    }

    @Override
    public void trackEvent(Event event) {
        Log.d(LOG_TAG, "Not yet implemented");
    }

    @Override
    public void batchEvent(Event event) {
        Log.d(LOG_TAG, "Not yet implemented");
    }

    @Override
    public void registerForPush() {
        Log.d(LOG_TAG, "Not yet implemented");
    }

    @Override
    public void disablePush() {
        Log.d(LOG_TAG, "Not yet implemented");
    }

    @Override
    public void logout() {
        Log.d(LOG_TAG, "Not yet implemented");
    }

}
