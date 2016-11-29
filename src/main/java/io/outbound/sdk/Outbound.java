package io.outbound.sdk;

import android.app.Application;

/**
 * Public interface for Outbound.
 *
 * <p>Outbound works with users and events. You tell Outbound about user using the {@link #identify(User)}
 * method. Events are sent using the {@link #track(Event)} method.</p>
 */
public class Outbound {
    private static WorkerThread worker;

    /**
     * Initialize the Outbound SDK.
     *
     * @param app instance of your Android {@link android.app.Application}
     * @param apiKey your Outbound environment's private API key
     * @param gcmSenderId your Google Developer project ID
     */
    public static void init(Application app, String apiKey, String gcmSenderId) {
        OutboundClient.init(app, apiKey, gcmSenderId);
        worker = new WorkerThread("outboundWorker");
        worker.start();
    }

    /**
     * Get the Active User's Token.
     * If there is no active user, then the value returned will be null.
     */
    public static String getActiveToken() {
        return OutboundClient.getInstance().fetchCurrentGCMToken();
    }

    /**
     * Identify the app user. Use {@link io.outbound.sdk.User.Builder} to build a user object
     * with all available attributes.
     *
     * <p>Identify should be called whenever your user logs in or any time information about the user
     * changes (for example, when they edit their profile).</p>
     *
     * @param user
     */
    public static void identify(final User user) {
        worker.post(new Runnable() {
            @Override
            public void run() {
                OutboundClient.getInstance().identify(user);
            }
        });
    }

    /**
     * Track an {@link Event} for the current user. An "event" is any action the user does that you want to
     * track. Obvious events might be "login", "register" or "add item to cart". Page views could also
     * be events that are track.
     *
     * <p>If you have not called {@link #identify(User)}, an anonymous user will be created and identified.
     * If/when you are able to identify the user, they will be aliased to the anonymous user automatically.</p>
     *
     * @param event
     */
    public static void track(final Event event) {
        worker.post(new Runnable() {
            @Override
            public void run() {
                OutboundClient.getInstance().track(event);
            }
        });
    }

    /**
     * Register the current user's GCM token with Outbound allowing Outbound to send the user push
     * notifications. As with {@link #track(Event)}, if the user has not been identified, an anonymous
     * user will be created.
     *
     * <p>Ideally you should never have to manually register a user. The device token is automatically
     * added to all {@link #identify(User)} calls and if you track an event before identifying, an
     * anonymous user is created (with a GCM token).</p>
     */
    public static void register() {
        worker.post(new Runnable() {
            @Override
            public void run() {
                OutboundClient.getInstance().register();
            }
        });
    }

    /**
     * Disable the current user's device token. This will prevent Outbound from sending push notifications
     * to the device until the token is reactivated.
     *
     * <p>Unlike {@link #register()} and {@link #track(Event)}, an anonymous user is <b>NOT</b> created
     * if a user has not been identified. In that case the disable call will simply exit without doing
     * anything.</p>
     *
     * <p>Like {@link #register()} you should not need to call disable. {@link #logout()} is the recommended
     * way of disabling a user's device token.</p>
     */
    public static void disable() {
        worker.post(new Runnable() {
            @Override
            public void run() {
                OutboundClient.getInstance().disable();
            }
        });
    }

    /**
     * If an anonymous user has been created <b>OR</b> a user was identified, logout will 1) disable
     * their GCM device token with Outbound and 2) clear the user from memory. The SDK then goes back
     * to an empty state. So if you call {@link #track(Event)} after calling logout, a new, anonymous
     * user will be created.
     */
    public static void logout() {
        worker.post(new Runnable() {
            @Override
            public void run() {
                OutboundClient.getInstance().logout();
            }
        });
    }

    /**
     * Called from admin activity to pair the device with an Outbound account.
     *
     * <p>Processed on whatever thread it is called from. DO NOT CALL on main thread.</p>
     * */
    public static boolean pairDevice(String pin) {
        return OutboundClient.getInstance().pairDevice(pin);
    }
}
