package com.zendesk.connect;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

/**
 * Responsible for intercepting touch events on the window to activate Connect admin mode.
 */
class TouchGestureMonitor implements Application.ActivityLifecycleCallbacks,
        TouchInterceptor.TouchInterceptionListener {
    private final Application application;
    private Activity foregroundActivity;

    private TouchGestureMonitor(Application application) {
        this.application = application;
    }

    static void add(Application application) {
        TouchGestureMonitor monitor = new TouchGestureMonitor(application);
        application.registerActivityLifecycleCallbacks(monitor);
    }

    static void remove(Application application) {
        TouchGestureMonitor monitor = new TouchGestureMonitor(application);
        application.unregisterActivityLifecycleCallbacks(monitor);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }

    @Override
    public void onActivityStarted(Activity activity) { }

    @Override
    public void onActivityResumed(Activity activity) {
        // Proxy the window callback;
        Window window = activity.getWindow();
        Window.Callback callback = window.getCallback();
        if (!(callback instanceof TouchInterceptor)) {
            window.setCallback(new TouchInterceptor(this, callback));
        } else {
            ((TouchInterceptor) callback).setListener(this);
        }

        this.foregroundActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // Remove proxy onPause;
        Window window = activity.getWindow();
        Window.Callback callback = window.getCallback();
        if (callback instanceof TouchInterceptor) {
            ((TouchInterceptor) callback).setListener(null);
        }

        this.foregroundActivity = null;
    }

    @Override
    public void onActivityStopped(Activity activity) { }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }

    @Override
    public void onActivityDestroyed(Activity activity) { }

    @Override
    public void onActivationGesture() {
        if (foregroundActivity == null) {
            throw new IllegalStateException("This should never reached if no activity is in the foreground.");
        }

        Intent intent = new Intent(application, AdminActivity.class);
        foregroundActivity.startActivity(intent);
    }
}
