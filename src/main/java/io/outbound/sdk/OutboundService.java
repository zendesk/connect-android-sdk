package io.outbound.sdk;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

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
 *      <action android:name="io.outbound.sdk.action.TRACK_NOTIF" />
 *      <action android:name="io.outbound.sdk.action.DISPLAY_NOTIF" />
 *      <action android:name="io.outbound.sdk.action.RECEIVED_NOTIF" />
 *      <action android:name="io.outbound.sdk.action.OPEN_NOTIF" />
 *   </intent-filter>
 * </service>}
 * </pre>
 *
 * <p>If you would like to implement your own tracking for notifications received (or for one of the other
 * actions), you should subclass it and implement on of the public methods.</p>
 *
 * <ul>
 *     <li>{@link #onDisplayNotification(PushNotification)} is called after a notification is displayed.</li>
 *     <li>{@link #onReceiveNotification(PushNotification)} is called after a notification is received. Important
 * to note the difference between display and receive. onDisplayNotification will only be called after
 * a notification is DISPLAYED. If you send silent notifications, this will not get called. onReceiveNotification
 * is called after every notification that is received whether it is displayed or not.</li>
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
    private static final String TAG = BuildConfig.APPLICATION_ID;

    // consumers package name should be prepended to each of these actions
    public static final String ACTION_OPEN_NOTIF = ".outbound.action.OPEN_NOTIF";
    public static final String ACTION_DISPLAY_NOTIF = ".outbound.action.DISPLAY_NOTIF";
    public static final String ACTION_RECEIVED_NOTIF = ".outbound.action.RECEIVED_NOTIF";
    public static final String ACTION_TRACK_NOTIF = ".outbound.action.TRACK_NOTIF";

    public static final String EXTRA_NOTIFICATION = BuildConfig.APPLICATION_ID + ".extra.NOTIFICATION";

    private NotificationManager notificationManager;

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
        String recvAction = pkgName + ACTION_RECEIVED_NOTIF;
        String displayAction = pkgName + ACTION_DISPLAY_NOTIF;
        String trackAction = pkgName + ACTION_TRACK_NOTIF;
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
        } else if (action.equals(recvAction)) {
            if (!notif.isTestMessage() && notif.getInstanceId() != null) {
                OutboundClient.getInstance().receiveNotification(notif.getInstanceId());
            }

            // TODO handle exceptions? thread?
            onReceiveNotification(notif);
        } else if (action.equals(displayAction)) {
            PackageManager pm = getPackageManager();
            ApplicationInfo appInfo = null;
            try {
                appInfo = pm.getApplicationInfo(getApplication().getPackageName(), PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                // since we are using context.getPackageName() this should never happen.
                Log.e(TAG, "Tried to access app that doesn't exist.");
            }

            int icon = appInfo.icon;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setAutoCancel(true)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(notif.getBody() == null ? "" : notif.getBody()))
                            .setContentText(notif.getBody() == null ? "" : notif.getBody())
                            .setContentTitle(notif.getTitle() == null ? "" : notif.getTitle())
                            .setSmallIcon(icon);

            try {
                String image = notif.getSmNotifImage();
                String folder = notif.getSmNotifFolder();
                if (folder != "" && image != "") {
                    int resId = getResources().getIdentifier(image, folder, getApplication().getPackageName());
                    if (resId != 0) {
                        builder.setSmallIcon(resId);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Small icon doesn't exist");
                builder.setSmallIcon(icon);
            }

            try {
                String image = notif.getLgNotifImage();
                String folder = notif.getLgNotifFolder();
                Resources rs = pm.getResourcesForApplication(getApplication().getPackageName());
                if (folder != "" && image != "") {
                    int resId = getResources().getIdentifier(image, folder, getApplication().getPackageName());
                    if (resId != 0) {
                        builder.setLargeIcon(BitmapFactory.decodeResource(rs, resId));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Large icon doesn't exist");
            }

            try {
                Uri media = null;
                if (notif.getSoundSilent().equals(true)){
                } else if (notif.getSoundDefault().equals(true)) {
                    media = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                } else {
                    int resId = getResources().getIdentifier(notif.getSoundFile(), notif.getSoundFolder(), getApplication().getPackageName());
                    if (resId != 0) {
                        media = Uri.parse("android.resource://" + getPackageName() + "/" + resId);
                    }
                }

                if (media != null) {
                    builder.setSound(media);
                }
            } catch (Exception e) {
                Log.e(TAG, "Music asset does not exist");
            }

            // we set local only since the server already sends to all device tokens registered.
            // until we investigate how this works it is better safe than sorry.
            builder.setLocalOnly(true);

            // TODO implement these
            // builder.setDeleteIntent(); // notification is cleared
            // builder.setPriority();
            // builder.setLights();
            // builder.setSound();

            Intent intentToOpen = new Intent(getPackageName() + OutboundService.ACTION_OPEN_NOTIF);
            intentToOpen.setPackage(getApplicationContext().getPackageName());
            intentToOpen.putExtra(OutboundService.EXTRA_NOTIFICATION, notif);

            PendingIntent pIntent = PendingIntent.getService(getApplicationContext(), 0, intentToOpen, PendingIntent.FLAG_ONE_SHOT);
            builder.setContentIntent(pIntent);

            if (notif.getCategory() != null) {
                builder.setCategory(notif.getCategory());
            }

            Notification notification = builder.build();

            if (notificationManager == null) {
                this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }

            notificationManager.notify(notif.getId(), notification);

            // TODO handle exceptions? thread?
            onDisplayNotification(notif);
        } else if (action.equals(trackAction)) {
            if (notif.getInstanceId() != null) {
                OutboundClient.getInstance().trackNotification(notif.getInstanceId());
            }
        }

        OutboundPushReceiver.completeWakefulIntent(intent);
    }

    /**
     * Called after an Outbound notification is displayed. <b>IS NOT</b> called for silent notifications
     * since they do not "display."
     *
     * @param notif
     */
    public void onDisplayNotification(PushNotification notif) {}

    /**
     * Called after an Outbound notification is clicked and opened. If you want to implement your own
     * logic for routing, links and/or what views open when a notification is clicked, this is where
     * you'd do it.
     *
     * @param notif
     */
    public void onOpenNotification(PushNotification notif) {}

    /**
     * Called after an Outbound notification is received. Called for <b>ALL</b> notifications whether
     * they display or not. This method is recommended for handling any custom payload properties
     * sent in the notification.
     *
     * @param notif
     */
    public void onReceiveNotification(PushNotification notif) {}
}
