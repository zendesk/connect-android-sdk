package com.zendesk.connect;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.zendesk.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * A class that manages listeners of {@link Application.ActivityLifecycleCallbacks} and notifies
 * any active listeners once the host app is foregrounded and its current {@link Activity} stays
 * in the resumed state for at least {@link #DELAY_AFTER_FOREGROUND_IN_MILLIS}.
 */
@ConnectScope
class ForegroundListener implements Application.ActivityLifecycleCallbacks {

    /**
     * Callback interface to be implemented by classes that want to listen to a foreground event.
     */
    interface Callback {

        /**
         * Called when the host app is foreground and its current {@link Activity} stays in the
         * resumed state for at least {@link #DELAY_AFTER_FOREGROUND_IN_MILLIS}.
         */
        void onForeground();
    }

    private static final String LOG_TAG = "ForegroundListener";

    /**
     * Arbitrary delay to give the OS the chance to finish any ongoing transitions before running
     * our own actions.
     */
    private static final long DELAY_AFTER_FOREGROUND_IN_MILLIS = 1000;

    /**
     * Flag that signals if the host app is in the foreground or not. This is updated based on
     * lifecycle events. Every time an {@link Activity} is stopped this is set to false, and every
     * time an {@link Activity} is resumed this is set to true.
     */
    @VisibleForTesting
    boolean isHostAppInTheForeground = false;

    /**
     * The {@link Class} of the last resumed {@link Activity}. This is updated based on
     * lifecycle events. Every time an {@link Activity} is stopped this is set to null, and every
     * time an {@link Activity} is resumed this is set to the class of that activity.
     */
    @Nullable
    @VisibleForTesting
    Class<? extends Activity> lastResumedActivityClass = null;

    /**
     * The list of {@link Callback}s that should be notified once the host app is foregrounded.
     */
    @VisibleForTesting
    final List<Callback> callbacks = new ArrayList<>();

    /**
     * The {@link Handler} responsible for posting the messages once the host app is foregrounded.
     * It is only assigned once an {@link Activity} in the host app is resumed.
     */
    @Nullable
    private Handler handler;

    /**
     * The {@link Runnable} that will run once the app is foregrounded and stays foregrounded for at
     * least {@link #DELAY_AFTER_FOREGROUND_IN_MILLIS}.
     * It is only assigned once an {@link Activity} in the host app is resumed.
     */
    @Nullable
    private Runnable runnable;

    @Inject
    ForegroundListener(Application application) {
        application.registerActivityLifecycleCallbacks(this);
    }

    /**
     * If the host app is currently in the foreground or not.
     *
     * @return true if the host app is in the foreground, false otherwise
     */
    boolean isHostAppInTheForeground() {
        return isHostAppInTheForeground;
    }

    /**
     * If clazz matches the {@link Class} of the last resumed activity.
     *
     * @param clazz the {@link Class} of the {@link Activity} to check
     * @return true if clazz matches the {@link Class} of the last resumed {@link Activity}, false otherwise
     */
    boolean isActivityLastResumed(Class<? extends Activity> clazz) {
        return lastResumedActivityClass != null && lastResumedActivityClass == clazz;
    }

    /**
     * Adds the given callback to the list of {@link Callback}s to be notified when the host app
     * is foregrounded.
     *
     * @param callback a class that implements {@link Callback}
     */
    void addCallback(Callback callback) {
        if (callbacks.contains(callback)) {
            Logger.d(LOG_TAG, "addCallback - Callback was already registered");
            return;
        }
        Logger.d(LOG_TAG, "addCallback - Adding callback");
        callbacks.add(callback);
    }

    /**
     * Removes the given callback from the list of {@link Callback}s to be notified when the host app
     * is foregrounded.
     *
     * @param callback a class that implements {@link Callback}
     */
    void removeCallback(Callback callback) {
        Logger.d(LOG_TAG, "removeCallback - Removing callback");
        callbacks.remove(callback);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // Intentionally empty
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // Intentionally empty
    }

    /**
     * Starts a {@link Runnable} that calls {@link #onForeground}. The runnable will be delayed by
     * {@link #DELAY_AFTER_FOREGROUND_IN_MILLIS}.
     *
     * @param activity the resumed {@link Activity}
     */
    @Override
    public void onActivityResumed(Activity activity) {
        Logger.d(LOG_TAG, "onActivityResumed - Starting runnable");

        isHostAppInTheForeground = true;
        lastResumedActivityClass = activity.getClass();

        if (!callbacks.isEmpty()) {
            setupHandler();
        }
    }

    /**
     * Service method to assign {@link #runnable} with a {@link Runnable} that will call
     * {@link #onForeground} once it runs, and {@link #handler} with a new instance of {@link Handler},
     * that will {@link Handler#postDelayed} the runnable.
     */
    @VisibleForTesting
    void setupHandler() {
        runnable = new Runnable() {
            @Override
            public void run() {
                onForeground();
            }
        };
        handler = new Handler();
        handler.postDelayed(runnable, DELAY_AFTER_FOREGROUND_IN_MILLIS);
    }

    /**
     * Remove any callbacks for a previously posted {@link #runnable}.
     *
     * @param activity the resumed {@link Activity}
     */
    @Override
    public void onActivityPaused(Activity activity) {
        Logger.d(LOG_TAG, "onActivityPaused - Removing runnable callbacks");

        isHostAppInTheForeground = false;
        lastResumedActivityClass = null;

        resetHandler();
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // Intentionally empty
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // Intentionally empty
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // Intentionally empty
    }

    /**
     * Invoked when the host app is foregrounded. It calls {@link Callback#onForeground} on each
     * item of {@link #callbacks}.
     */
    @VisibleForTesting
    void onForeground() {
        Logger.d(LOG_TAG, "onForeground - Alerting listeners and unregistering lifecycle callbacks");

        List<Callback> currentCallbacks = new ArrayList<>(callbacks);
        for (Callback callback : currentCallbacks) {
            callback.onForeground();
        }
        resetHandler();
    }

    /**
     * Service method to remove pending messages from {@link #runnable} to the {@link #handler}.
     * Also nullifies {@link #handler} and {@link #runnable}.
     */
    @VisibleForTesting
    void resetHandler() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        handler = null;
        runnable = null;
    }
}
