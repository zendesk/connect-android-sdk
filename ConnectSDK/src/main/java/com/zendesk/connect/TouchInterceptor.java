package com.zendesk.connect;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by jophde on 5/1/15.
 *
 * {@link android.view.Window.Callback} that receives the host app's touch events. Responsible
 * for determining if the admin mode should be enabled.
 */
public class TouchInterceptor implements Window.Callback {

    private static final int ACTIVATION_GESTURE_POINTER_COUNT = 4;
    private static final int ACTIVATION_GESTURE = 1;
    private static final int ACTIVATION_GESTURE_TIMEOUT = 8000;
    private final Window.Callback proxy;
    private TouchInterceptionListener listener;
    private TouchInterceptionHandler handler;

    public TouchInterceptor(TouchInterceptionListener listener, Window.Callback localCallback) {
        this.proxy = localCallback;
        this.listener = listener;
        this.handler = new TouchInterceptionHandler(this);
    }

    TouchInterceptionListener getListener() {
        return listener;
    }

    void setListener(TouchInterceptionListener listener) {
        this.listener = listener;
    }

    /**
     * Processes the given {@link MotionEvent}
     *
     * @param event the {@link MotionEvent} to be processed
     */
    private void process(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == ACTIVATION_GESTURE_POINTER_COUNT) {
                    handler.sendEmptyMessageAtTime(ACTIVATION_GESTURE,
                            event.getDownTime() + ACTIVATION_GESTURE_TIMEOUT);
                }

                break;
            case MotionEvent.ACTION_POINTER_UP:
                // Pointer hasn't been removed yet so use <=
                if (handler.hasMessages(ACTIVATION_GESTURE)
                        && (event.getPointerCount() <= ACTIVATION_GESTURE_POINTER_COUNT)) {
                    handler.removeMessages(ACTIVATION_GESTURE);
                }
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        return proxy.dispatchKeyEvent(keyEvent);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent keyEvent) {
        return proxy.dispatchKeyShortcutEvent(keyEvent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        process(motionEvent);
        return proxy.dispatchTouchEvent(motionEvent);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent motionEvent) {
        return proxy.dispatchTrackballEvent(motionEvent);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent motionEvent) {
        return proxy.dispatchGenericMotionEvent(motionEvent);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        return proxy.dispatchPopulateAccessibilityEvent(accessibilityEvent);
    }

    @Nullable
    @Override
    public View onCreatePanelView(int featureId) {
        return proxy.onCreatePanelView(featureId);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return proxy.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return proxy.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return proxy.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem menuItem) {
        return proxy.onMenuItemSelected(featureId, menuItem);
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams layoutParams) {
        proxy.onWindowAttributesChanged(layoutParams);
    }

    @Override
    public void onContentChanged() {
        proxy.onContentChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        proxy.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onAttachedToWindow() {
        proxy.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        proxy.onDetachedFromWindow();
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        proxy.onPanelClosed(featureId, menu);
    }

    @Override
    public boolean onSearchRequested() {
        return proxy.onSearchRequested();
    }

    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            return proxy.onSearchRequested(searchEvent);
        } else {
            return proxy.onSearchRequested();
        }
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return proxy.onWindowStartingActionMode(callback);
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int featureId) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            return proxy.onWindowStartingActionMode(callback, featureId);
        } else  {
            return proxy.onWindowStartingActionMode(callback);
        }
    }

    @Override
    public void onActionModeStarted(ActionMode actionMode) {
        proxy.onActionModeStarted(actionMode);
    }

    @Override
    public void onActionModeFinished(ActionMode actionMode) {
        proxy.onActionModeFinished(actionMode);
    }

    interface TouchInterceptionListener {
        void onActivationGesture();
    }

    static class TouchInterceptionHandler extends Handler {
        private TouchInterceptor interceptor;

        TouchInterceptionHandler(TouchInterceptor interceptor) {
            this.interceptor = interceptor;
        }

        @Override
        public void handleMessage(Message msg) {
            TouchInterceptionListener listener = interceptor.getListener();
            if (listener == null) {
                return;
            }

            switch (msg.what) {
                case ACTIVATION_GESTURE:
                    listener.onActivationGesture();
                    break;
            }
        }
    }
}
