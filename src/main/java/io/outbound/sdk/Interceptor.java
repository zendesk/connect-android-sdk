package io.outbound.sdk;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by jophde on 5/1/15.
 *
 * {@link android.view.Window.Callback} that receives the host app's touch events. Responsible
 * for determining if the admin mode should be enabled.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class Interceptor implements Window.Callback {

    public static final int ACTIVATION_GESTURE_POINTER_COUNT = 4;
    public static final int ACTIVATION_GESTURE = 1;
    private static final int ACTIVATION_GESTURE_TIMEOUT = 8000;
    private final Window.Callback proxy;
    private final Context context;
    private OnInterceptionListener listener;
    private OutboundHandler handler;

    public Interceptor(Context context, OnInterceptionListener listener, Window.Callback localCallback) {
        this.context = context;
        this.proxy = localCallback;
        this.listener = listener;
        this.handler = new OutboundHandler(this);
    }

    public OnInterceptionListener getListener() {
        return listener;
    }

    public void setListener(OnInterceptionListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return proxy.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(@NonNull KeyEvent event) {
        return proxy.dispatchKeyShortcutEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        process(event);
        return proxy.dispatchTouchEvent(event);
    }

    private void process(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == ACTIVATION_GESTURE_POINTER_COUNT) {
                    handler.sendEmptyMessageAtTime(ACTIVATION_GESTURE, event.getDownTime() + ACTIVATION_GESTURE_TIMEOUT);
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
    public boolean dispatchTrackballEvent(@NonNull MotionEvent event) {
        return proxy.dispatchTrackballEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return proxy.dispatchGenericMotionEvent(event);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(@NonNull AccessibilityEvent event) {
        return proxy.dispatchPopulateAccessibilityEvent(event);
    }

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
    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
        return proxy.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        proxy.onWindowAttributesChanged(attrs);
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
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return proxy.onWindowStartingActionMode(callback);
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        proxy.onActionModeStarted(mode);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        proxy.onActionModeFinished(mode);
    }

    public interface OnInterceptionListener {
        void onActivationGesture();
    }

    static class OutboundHandler extends Handler {
        private Interceptor interceptor;

        OutboundHandler(Interceptor interceptor) {
            this.interceptor = interceptor;
        }

        @Override public void handleMessage(Message msg) {
            if (interceptor.getListener() == null) return;

            switch (msg.what) {
                case ACTIVATION_GESTURE:
                    interceptor.getListener().onActivationGesture();
                    break;
            }
        }
    }
}