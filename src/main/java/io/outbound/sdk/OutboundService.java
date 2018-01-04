package io.outbound.sdk;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * OutboundService handles tasks related to notifications that Outbound sends. OutboundService or a
 * subclass of it should be registered as a service in your AndroidManifest.xml file.
 *
 * <p></p>
 *
 * <pre>
 * {@code
 * <service
 *   android:name="io.outbound.sdk.OutboundService"
 *   android:label="OutboundService" >
 *   <intent-filter>
 *      <action android:name="io.outbound.sdk.action.OPEN_NOTIF" />
 *   </intent-filter>
 * </service>}
 * </pre>
 *
 * <p>If you would like to implement your own tracking for notifications received (or for one of the other
 * actions), you should subclass it and implement on of the public methods.</p>
 *
 * <ul>
 *     <li>{@link #onOpenNotification(PushNotification)} is called after a notification is clicked.</li>
 * </ul>
 *
 * <p>There are 3 instances variables you can override which determine how the SDK handles deeplinking
 * and opening the app when a notification is clicked.</p>
 *
 * <ul>
 *     <li>{@link #handleDeeplinks} defaults to <b>true</b> and tells the SDK to handle any deeplinks
 *     present in the notification. If you override to false then clicking on an notification will
 *     <b>NOT</b> open the app. You will need to implement your own logic for handle notifications
 *     that have a link. You should do this in the {@link #onOpenNotification(PushNotification)} method.</li>
 *     <li>{@link #fallBackToMainActivity} defaults to <b>true</b>. If handleDeeplinks is true and the
 *     link does not resolve, the SDK will fall back to opening the main activity. You can turn this behavior
 *     off by setting fallBackToMainActivity to false.</li>
 *     <li>{@link #openMainActivityByDefault} defaults <b>true</b>. When the notification <b>DOES NOT</b>
 *     contain a deeplink, this tells the SDK to open the main activity when the notification is clicked.
 *     Again, you can turn this behavior off by setting openMainAcitvityByDefault to false in your subclass
 *     of OoutboundService.</li>
 * </ul>
 *
 * <p>In your implementation of {@link #onOpenNotification(PushNotification)}, you can find out what Outbound
 * did by calling the {@link PushNotification#linkHandled} and {@link PushNotification#mainActivityLaunched}
 * methods on the PushNotification object passed in.</p>
 */
public class OutboundService extends IntentService {

    // consumers package name should be prepended to each of these actions
    public static final String ACTION_OPEN_NOTIF = ".outbound.action.OPEN_NOTIF";

    public static final String EXTRA_NOTIFICATION = BuildConfig.APPLICATION_ID + ".extra.NOTIFICATION";

    protected boolean openMainActivityByDefault = true;
    protected boolean handleDeeplinks = true;
    protected boolean fallBackToMainActivity = true;

    public OutboundService() {
        this(OutboundService.class.getSimpleName());
    }

    public OutboundService(String name) {
        super(name);
    }

    @Override public void onCreate() {
        super.onCreate();
    }

    @Override
    protected final void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action == null) {
                throw new IllegalArgumentException("Intent has null action");
            }

            if (intent.hasExtra(EXTRA_NOTIFICATION)) {
                handleOutboundNotification(intent);
            }
        }
    }

    private void handleOutboundNotification(Intent intent) {
        PushNotification notif = intent.getParcelableExtra(EXTRA_NOTIFICATION);

        String pkgName = getPackageName();
        String action = intent.getAction();
        String openAction = pkgName + ACTION_OPEN_NOTIF;
        if (action.equals(openAction)) {
            if (!notif.isTestMessage()) {
                if (notif.getInstanceId() != null) {
                    OutboundClient.getInstance().openNotification(notif.getInstanceId());
                }
            }

            Context context = getApplicationContext();
            Intent linkIntent = null;

            if (notif.getDeeplink() != null) {
                if (handleDeeplinks) {
                    Intent tempIntent = new Intent(Intent.ACTION_VIEW);
                    tempIntent.setData(Uri.parse(notif.getDeeplink()));
                    tempIntent.setPackage(context.getPackageName());

                    PackageManager pm = context.getPackageManager();
                    ComponentName component = tempIntent.resolveActivity(pm);
                    if (component != null) {
                        linkIntent = tempIntent;
                        notif.setLinkHandled();
                    } else if (fallBackToMainActivity) {
                        final PackageManager manager = context.getPackageManager();
                        tempIntent = manager.getLaunchIntentForPackage(context.getPackageName());
                        if (tempIntent != null) {
                            linkIntent = tempIntent;
                            notif.setMainActivityLaunched();
                        }
                    }
                }
            } else if (openMainActivityByDefault) {
                final PackageManager manager = context.getPackageManager();
                Intent tempIntent = manager.getLaunchIntentForPackage(context.getPackageName());
                if (tempIntent != null) {
                    linkIntent = tempIntent;
                    notif.setMainActivityLaunched();
                }
            }

            if (linkIntent != null) {
                linkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(linkIntent);
            }

            // TODO handle exceptions? thread?
            onOpenNotification(notif);
        }
    }

    /**
     * Called after an Outbound notification is clicked and opened. If you want to implement your own
     * logic for routing, links and/or what views open when a notification is clicked, this is where
     * you'd do it.
     *
     * @param notif
     */
    public void onOpenNotification(PushNotification notif) {}
}
