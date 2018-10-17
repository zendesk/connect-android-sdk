package io.outbound.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Window;

import com.google.gson.Gson;
import com.zendesk.connect.Connect;
import com.zendesk.connect.EventFactory;
import com.zendesk.connect.UserBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.TimeZone;

import io.outbound.sdk.activity.AdminActivity;
import okhttp3.OkHttpClient;
import okhttp3.Response;

class OutboundClient {

    private static OutboundClient INSTANCE;

    private static final String TAG = BuildConfig.APPLICATION_ID;

    private Application app;
    private String notificationChannelId;

    private RequestHandler handler;

    private boolean offline = false;

    private boolean testMode = false;

    public synchronized static OutboundClient getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Outbound has not been initialized.");
        }
        return INSTANCE;
    }

    public synchronized static void init(Application app, String apiKey, String notificationChannelId) {
        INSTANCE = new OutboundClient(app, apiKey, notificationChannelId, null);
    }

    @VisibleForTesting
    synchronized static void initForTesting(Application app, String apiKey,
                                            String notificationChannelId, OkHttpClient testClient) {
        INSTANCE = new OutboundClient(app, apiKey, notificationChannelId, testClient);
        INSTANCE.testMode = true;
    }

    private OutboundClient(Application app, String apiKey,
                           String notificationChannelId, OkHttpClient testClient) {

        this.app = app;
        this.notificationChannelId = notificationChannelId;

        Monitor.add(app);

        Gson gson = new Gson();

        if (testClient == null) {
            Connect.INSTANCE.init(app.getApplicationContext(), apiKey);
            this.handler = new RequestHandler("outboundRequestWorker", app, apiKey, gson);
        } else {
            this.handler = new RequestHandler("outboundRequestWorker", app, apiKey,
                    gson, testClient);
        }
        handler.start();

        if (!isConnected()) {
            updateConnectionStatus();
        } else {
            handler.setReadyState(true);
        }
    }

    public synchronized void identify(User user) {
        if (user == null) {
            Log.d(TAG, "Couldn't identify a null user");
            return;
        }

        com.zendesk.connect.User connectUser = new UserBuilder(user.getUserId())
                .setPreviousId(user.getPreviousId())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setEmail(user.getEmail())
                .setPhoneNumber(user.getPhoneNumber())
                .setUserAttributes(user.getAttributes())
                .setGroupId(user.getGroupId())
                .setGroupAttributes(user.getGroupAttributes())
                .setFcmToken(user.getFcmToken())
                .setTimezone(TimeZone.getDefault().getID())
                .build();

        Connect.INSTANCE.identifyUser(connectUser);
    }

    public void track(Event event) {
        if (event == null) {
            Log.d(TAG, "Couldn't track a null event");
        }
        com.zendesk.connect.Event connectEvent =
                EventFactory.createEvent(event.getName(), event.getProperties());

        Connect.INSTANCE.trackEvent(connectEvent);
    }

    public void register() {
        Connect.INSTANCE.registerForPush();
    }

    public void disable() {
        Connect.INSTANCE.disablePush();
    }

    public void logout() {
        Connect.INSTANCE.logout();
    }

    public boolean pairDevice(String pin) {
        if (!Connect.INSTANCE.isEnabled()) {
            return false;
        }

        boolean paired = false;

        String token = getFcmToken();
        if (token == null || token.isEmpty()) {
            Log.d(TAG, "Device token could not be retrieved");
            return false;
        }

        try {
            JSONObject payload;
            payload = new JSONObject();
            payload.put("code", Integer.parseInt(pin));
            payload.put("deviceToken", token);
            payload.put("deviceName", Build.MANUFACTURER + " " + Build.MODEL);

            OutboundRequest request = new OutboundRequest(OutboundRequest.Type.PAIR, payload.toString());
            Response response = handler.sendRequest(request);
            if (response.isSuccessful()) {
                paired = true;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Couldn't create basic JSON object for register payload.", e);
        } catch (IOException e) {
            if (!isConnected()) {
                updateConnectionStatus();
            }
        }
        return paired;
    }


    /**
     * Gets the FCM token
     *
     * If the SDK was initialised using initForTesting then a dummy token is returned
     *
     * @return the FCM token, or null if we could not find it.
     */
    @Nullable private String getFcmToken() {
        if (testMode) {
            return "test_token";
        }

        com.zendesk.connect.User user = Connect.INSTANCE.getActiveUser();

        return user != null ? user.getFcmToken() : null;
    }

    public void receiveNotification(String instanceId) {
        if (!Connect.INSTANCE.isEnabled()) {
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("_oid", instanceId);

            handler.queue(new OutboundRequest(OutboundRequest.Type.RECEIVE, payload.toString()));
        } catch (JSONException e) {
            Log.e(TAG, "Couldn't create basic JSON object for received notification payload.", e);
        }
    }

    public void openNotification(String instanceId) {
        if (!Connect.INSTANCE.isEnabled()) {
            return;
        }

        try {
            JSONObject payload;
            payload = new JSONObject();
            payload.put("_oid", instanceId);

            handler.queue(new OutboundRequest(OutboundRequest.Type.OPEN, payload.toString()));
        } catch (JSONException e) {
            Log.e(TAG, "Couldn't create basic JSON object for opened notification payload.", e);
        }
    }

    public void trackNotification(Context ctx, String instanceId) {
        if (!Connect.INSTANCE.isEnabled()) {
            return;
        }

        try {
            JSONObject payload = new JSONObject();

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ctx);
            boolean notificationsRevoked = !notificationManagerCompat.areNotificationsEnabled();

            payload.put("i", instanceId);
            payload.put("revoked", notificationsRevoked);

            handler.queue(new OutboundRequest(OutboundRequest.Type.TRACKER, payload.toString()));
        } catch (JSONException e) {
            Log.e(TAG, "Couldn't create basic JSON object for tracker callback payload.", e);
        }
    }

    // Synchronized method means that if the object that
    // it is currently using, all methods MUST persists through
    // In this case, since it is called after IDENTIFY
    // It will wait for the FCM token to be put on the active user.
    public synchronized String fetchCurrentFcmToken() {
        com.zendesk.connect.User user = Connect.INSTANCE.getActiveUser();

        return user != null ? user.getFcmToken() : "";
    }

    public synchronized void updateConnectionStatus() {
        boolean currentlyOffline = !isConnected();
        boolean wasOnline = !offline;
        this.offline = currentlyOffline;

        if (wasOnline && offline) {
            monitorConnection();
            handler.setConnectionStatus(false);
        } else if (!offline) {
            stopMonitoringConnection();
        }
    }

    synchronized void checkEnabled() {
        Boolean enabled = Connect.INSTANCE.isEnabled();

        if (!enabled && Monitor.monitorActive) {
            Monitor.remove(app);
        } else if (enabled && !Monitor.monitorActive) {
            Monitor.add(app);
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            try {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            } catch(SecurityException e) {
                // This happens when the user manages
                // to revoke the permission to the network access
                // So we assume the network exists
                return true;
            }
        }

        return false;
    }

    private synchronized void monitorConnection() {
        PackageManager pm = app.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(app, ConnectionReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private synchronized void stopMonitoringConnection() {
        PackageManager pm = app.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(app, ConnectionReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public String getNotificationChannelId() {
        return notificationChannelId;
    }

    /**
     * Responsible for intercepting touch events on the window to activate Outbound admin mode.
     * This only works on API >= 14 (Ice Cream Sandwich).
     */
    static class Monitor implements Application.ActivityLifecycleCallbacks, Interceptor.OnInterceptionListener {
        private final Application application;
        private Activity foregroundActivity;
        private static boolean monitorActive = false;

        Monitor(Application application) {
            this.application = application;
        }

        static void add(Application application) {

            Monitor monitor = new Monitor(application);
            application.registerActivityLifecycleCallbacks(monitor);
            monitorActive = true;


            // No admin panel for users pre ICS :(.  There just isn't any way else to do it
            // that wouldn't require much more implementation.  Fortunately, ICS is 90%
            // devices at least.
        }

        static void remove(Application application) {

            Monitor monitor = new Monitor(application);
            application.unregisterActivityLifecycleCallbacks(monitor);
            monitorActive = false;

        }

        @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override public void onActivityStarted(Activity activity) {
        }

        @Override public void onActivityResumed(Activity activity) {
            // Proxy the window callback;
            Window window = activity.getWindow();
            Window.Callback callback = window.getCallback();
            if (!Interceptor.class.isInstance(callback)) {
                window.setCallback(new Interceptor(activity, this, callback));
            } else if (Interceptor.class.isInstance(callback)) {
                ((Interceptor) callback).setListener(this);
            }

            this.foregroundActivity = activity;
        }

        @Override public void onActivityPaused(Activity activity) {
            // Remove proxy onPause;
            Window window = activity.getWindow();
            Window.Callback callback = window.getCallback();
            if (Interceptor.class.isInstance(callback)) {
                ((Interceptor) callback).setListener(null);
            }

            this.foregroundActivity = null;
        }

        @Override public void onActivityStopped(Activity activity) {
        }

        @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override public void onActivityDestroyed(Activity activity) {
        }

        @Override public void onActivationGesture() {
            if (foregroundActivity == null) {
                throw new IllegalStateException("This should never reached if no activity is in the foreground.");
            }

            Intent intent = new Intent(application, AdminActivity.class);
            foregroundActivity.startActivity(intent);
        }
    }
}
