package io.outbound.sdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.outbound.sdk.activity.AdminActivity;

class OutboundClient {

    private static OutboundClient INSTANCE;

    private static final String TAG = BuildConfig.APPLICATION_ID;

    private static final String PREFS_NAME = BuildConfig.APPLICATION_ID + ".prefs";
    private static final String PREFS_CONFIG = BuildConfig.APPLICATION_ID + "prefs.config";
    private static final String PREFS_USER = BuildConfig.APPLICATION_ID + "prefs.user";

    private Application app;
    private String apiKey;
    private String gcmSenderId;

    private Gson gson;
    private SharedPreferences preferences;
    private RequestHandler handler;

    private User activeUser;

    private boolean configLoaded = false;
    private boolean offline = false;
    private boolean enabled = true;

    public synchronized static OutboundClient getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Outbound has not been initialized.");
        }
        return INSTANCE;
    }

    public synchronized static void init(Application app, String apiKey, String gcmSenderId) {
        INSTANCE = new OutboundClient(app, apiKey, gcmSenderId);
    }

    private OutboundClient(Application app, String apiKey, String gcmSenderId) {
        this.app = app;
        this.apiKey = apiKey;
        this.gcmSenderId = gcmSenderId;

        Monitor.add(app);

        this.handler = new RequestHandler("outboundRequestWorker", app, apiKey);
        handler.start();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        this.gson = gsonBuilder.create();

        this.preferences = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (preferences.contains(PREFS_USER)) {
            String userData = preferences.getString(PREFS_USER, "");
            if (userData != null) {
                this.activeUser = gson.fromJson(userData, User.class);
            }
        }
        this.configLoaded = preferences.contains(PREFS_CONFIG);

        if (!isConnected()) {
            updateConnectionStatus();
        } else if (!configLoaded) {
            loadConfig();
        } else {
            handler.setReadyState(true);
        }
    }

    public synchronized void identify(User user) {
        if (!enabled) {
            return;
        }

        setUser(user, true);
    }

    public void track(Event event) {
        if (!enabled) {
            return;
        }

        ensureUser();
        event.setUserId(activeUser.getUserId(), activeUser.isAnonymous());
        handler.queue(new OutboundRequest(OutboundRequest.Type.TRACK, gson.toJson(event)));
    }

    public void register() {
        register(null);
    }

    public void register(String tokenToReplace) {
        if (!enabled) {
            return;
        }

        ensureUser(false);

        // can't register a user if they don't have a token.
        // ensureUser called above would have loaded the token
        if (!activeUser.hasGcmToken()) {
            Log.d(TAG, "could not register user with no token");
            return;
        }

        try {
            JSONObject payload;
            payload = new JSONObject();
            payload.put("user_id", activeUser.getUserId());
            payload.put("token", activeUser.getGcmToken());

            if (tokenToReplace != null) {
                payload.put("replace", tokenToReplace);
            }

            handler.queue(new OutboundRequest(OutboundRequest.Type.REGISTER, payload.toString()));
        } catch (JSONException e) {
            Log.e(TAG, "Couldn't create basic JSON object for register payload.", e);
        }
    }

    public void disable() {
        if (!enabled) {
            return;
        }

        // can't disable a user that we don't know about.
        // don't want to generate anon user here either since we only need to disable
        // users who have been identified. anon users don't exist yet so no point in disabling.
        if (activeUser == null) {
            return;
        }

        if (!activeUser.hasGcmToken()) {
            Log.d(TAG, "can not disable user with no token");
            return;
        }

        try {
            JSONObject payload;
            payload = new JSONObject();
            payload.put("user_id", activeUser.getUserId());
            payload.put("token", activeUser.getGcmToken());

            handler.queue(new OutboundRequest(OutboundRequest.Type.DISABLE, payload.toString()));
        } catch (JSONException e) {
            Log.e(TAG, "Couldn't create basic JSON object for disable payload.", e);
        }
    }

    public void logout() {
        if (!enabled) {
            return;
        }

        if (activeUser == null) {
            return;
        }

        this.activeUser = null;
    }

    public boolean pairDevice(String pin) {
        if (!enabled) {
            return false;
        }

        boolean paired = false;

        try {
            JSONObject payload;
            payload = new JSONObject();
            payload.put("code", Integer.parseInt(pin));
            payload.put("deviceToken", getGcmToken());
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


    private String getGcmToken() {
        if (!enabled) {
            return null;
        }

        String token = null;
        FirebaseInstanceId iid = FirebaseInstanceId.getInstance();
        token = iid.getToken();
        if (token == "") {
            Log.e(TAG, "Error getting Firebase token");
        }

        return token;
    }

    public void setConfig(String config) {
        preferences.edit().putString(PREFS_CONFIG, config).apply();
        configLoaded = true;
        checkEnabled();
        handler.setReadyState(true);
    }

    public void loadConfig(int attempts) {
        if (!enabled) {
            return;
        }

        OutboundRequest request = new OutboundRequest(OutboundRequest.Type.CONFIG, apiKey, attempts);
        if (request.getAttempts() == 0) {
            handler.processNow(request);
        } else {
            handler.processAfterDelay(request, request.getAttempts() * request.getAttempts() * 1000);
        }
    }

    public void loadConfig() {
        if (!enabled) {
            return;
        }

        loadConfig(0);
    }

    public void receiveNotification(String instanceId) {
        if (!enabled) {
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
        if (!enabled) {
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

    public void trackNotification(String instanceId) {
        if (!enabled) {
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("i", instanceId);
            payload.put("revoked", false);

            handler.queue(new OutboundRequest(OutboundRequest.Type.TRACKER, payload.toString()));
        } catch (JSONException e) {
            Log.e(TAG, "Couldn't create basic JSON object for tracker callback payload.", e);
        }
    }

    // Synchronized method means that if the object that
    // it is currently using, all methods MUST persists through
    // In this case, since it is called after IDENTIFY
    // It will wait for the GCM token to be put on the active user.
    public synchronized String fetchCurrentGCMToken() {
        if (activeUser == null) {
            return "";
        }

        return activeUser.getGcmToken();
    }

    public void refreshFCMToken() {
        if (!enabled) {
            return;
        }

        refreshFCMToken(false);
    }

    public void refreshFCMToken(boolean registerIfNew) {
        if (!enabled) {
            return;
        }

        if (activeUser == null) {
            return;
        }

        String newToken = getGcmToken();
        if (newToken != null && (!activeUser.hasGcmToken() || !activeUser.getGcmToken().equals(newToken))) {
            String currentToken = activeUser.getGcmToken();
            activeUser.setGcmToken(newToken);

            if (registerIfNew) {
                register(currentToken);
            }
        }
    }

    public synchronized void updateConnectionStatus() {
        boolean currentlyOffline = !isConnected();
        boolean wasOnline = !offline;
        this.offline = currentlyOffline;

        // if not previously online, but now connected
        // and this is the first time we've been online, load the conig.
        if (!wasOnline && !offline && !configLoaded) {
            loadConfig();
        }

        if (wasOnline && offline) {
            monitorConnection();
            handler.setConnectionStatus(false);
        } else if (!offline) {
            stopMonitoringConnection();
        }
    }

    /** Ensure that a user exists AND identify them if creating an anonymous user. */
    private void ensureUser() {
        ensureUser(true);
    }

    /**
     * Ensure we have an active user object.
     *
     * @param identify if true, the user will automatically be identified.
     */
    private synchronized void ensureUser(boolean identify) {
        if (activeUser != null) {
            return;
        }

        setUser(User.newAnonymousUser(), identify);
    }

    private void sendIdentifyRequest() {
        handler.queue(new OutboundRequest(OutboundRequest.Type.IDENTIFY, gson.toJson(activeUser)));
        if (activeUser.getPreviousId() != null) {
            activeUser.setPrevioudId(null);
            persistUser();
        }
    }

    /** Set the active user object and DO NOT identify. */
    private void setUser(User user) {
        setUser(user, false);
    }

    /**
     * Set the active user object.
     *
     * @param user new active user
     * @param identify if true and the user is new (first user seen or different than current
     *                        user, the user will automatically be identified.
     */
    private synchronized void setUser(User user, boolean identify) {
        if (activeUser != null) {
            if (!user.getUserId().equals(activeUser.getUserId())) {
                if (activeUser.isAnonymous()) {
                    user.setPrevioudId(activeUser.getUserId());
                }
            }
        }

        this.activeUser = user;
        refreshFCMToken();

        persistUser();

        if (identify) {
            sendIdentifyRequest();
        }
    }

    private synchronized void persistUser() {
        if (activeUser != null) {
            preferences.edit().putString(PREFS_USER, gson.toJson(activeUser)).apply();
        } else {
            preferences.edit().remove(PREFS_USER);
        }
    }

    private synchronized void checkEnabled() {
        if (configLoaded) {
            String cfgStr = preferences.getString(PREFS_CONFIG, "");
            if (!cfgStr.equals("")) {
                boolean wasEnabled = enabled;

                try {
                    JSONObject cfg = new JSONObject(cfgStr);
                    if (cfg.has("enabled")) {
                        this.enabled = cfg.getBoolean("enabled");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error processing config.", e);
                }

                if (wasEnabled && !enabled) {
                    Monitor.remove(app);
                } else if (!wasEnabled && enabled) {
                    Monitor.add(app);
                }
            }
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
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

    /**
     * Responsible for intercepting touch events on the window to activate Outbound admin mode.
     * This only works on API >= 14 (Ice Cream Sandwich).
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    static class Monitor implements Application.ActivityLifecycleCallbacks, Interceptor.OnInterceptionListener {
        private final Application application;
        private Activity foregroundActivity;

        Monitor(Application application) {
            this.application = application;
        }

        static void add(Application application) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                Monitor monitor = new Monitor(application);
                application.registerActivityLifecycleCallbacks(monitor);
            }

            // No admin panel for users pre ICS :(.  There just isn't any way else to do it
            // that wouldn't require much more implementation.  Fortunately, ICS is 90%
            // devices at least.
        }

        static void remove(Application application) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                Monitor monitor = new Monitor(application);
                application.unregisterActivityLifecycleCallbacks(monitor);
            }
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
