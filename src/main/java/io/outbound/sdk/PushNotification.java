package io.outbound.sdk;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * The PushNotification represents one notification send from Outbound to the device. A PushNotification
 * is passed into the overridable methods of {@link OutboundService} allowing you to implement your own
 * logic around notifications.
 */
public class PushNotification implements Parcelable {
    private final static String TAG = BuildConfig.APPLICATION_ID;

    private final static String SILENT_FIELD = "_oq";
    private final static String UNINSTALL_TRACKER_FIELD = "_ogp";
    private final static String TEST_FIELD = "_otm";
    private final static String ID_FIELD = "_onid";
    private final static String INSTANCE_ID_FIELD = "_oid";
    private final static String LINK_FIELD = "_odl";

    private final static String SOUND_FILE_FIELD = "_sf";
    private final static String SOUND_FOLDER_FIELD = "_sfo";
    private final static String SOUND_SILENT = "_silent";
    private final static String SOUND_DEFAULT = "_soundDefault";
    private final static String TITLE_FIELD = "title";
    private final static String BODY_FIELD = "body";
    private final static String CATEGORY_FIELD = "category";
    private final static String PAYLOAD_FIELD = "payload";
    private final static String LG_NOTIF_IMG_FIELD = "_lni";
    private final static String LG_NOTIF_FOLDER_FIELD = "_lnf";
    private final static String SM_NOTIF_IMG_FIELD = "_sni";
    private final static String SM_NOTIF_FOLDER_FIELD = "_snf";

    private boolean uninstallTracker;
    private boolean test;
    private boolean silent;

    private boolean linkHandled = false;
    private  boolean mainActivityLaunched = false;

    private int id;
    private String instanceId;
    private String link;
    private String title;
    private String body;
    private String category;
    private String lgNotifFolder;
    private String lgNotifImage;
    private String smNotifFolder;
    private String smNotifImage;
    private Boolean soundSilent = false;
    private Boolean soundDefault = false;
    private String soundFile;
    private String soundFolder;
    private JSONObject payload;

    /** Required methods to implement {@link android.os.Parcelable} */
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PushNotification createFromParcel(Parcel in) {
            return new PushNotification(in);
        }

        public PushNotification[] newArray(int size) {
            return new PushNotification[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle data = new Bundle();

        data.putString(SILENT_FIELD, silent ? "true" : "false");
        data.putString(UNINSTALL_TRACKER_FIELD, uninstallTracker ? "true" : "false");
        data.putString(TEST_FIELD, test ? "true" : "false");
        data.putString(ID_FIELD, id + "");
        data.putString(INSTANCE_ID_FIELD, instanceId);
        data.putString(TITLE_FIELD, title);
        data.putString(BODY_FIELD, body);
        data.putString(CATEGORY_FIELD, category);
        data.putString(LINK_FIELD, link);
        data.putString(LG_NOTIF_FOLDER_FIELD, lgNotifFolder);
        data.putString(LG_NOTIF_IMG_FIELD, lgNotifImage);
        data.putString(SM_NOTIF_FOLDER_FIELD, smNotifFolder);
        data.putString(SM_NOTIF_IMG_FIELD, smNotifImage);
        data.putString(SOUND_DEFAULT, soundDefault ? "true" : "false");
        data.putString(SOUND_SILENT, soundSilent ? "true" : "false");
        data.putString(SOUND_FILE_FIELD, soundFile);
        data.putString(SOUND_FOLDER_FIELD, soundFolder);
        if (payload != null) {
            data.putString(PAYLOAD_FIELD, payload.toString());
        }

        dest.writeBundle(data);
    }

    public PushNotification(Parcel in) {
        this(in.readBundle());
    }

    public PushNotification(Bundle data) {
        Set<String> keys = data.keySet();
        for(String key : keys) {
            switch (key) {
                case INSTANCE_ID_FIELD:
                    this.instanceId = data.getString(key);
                    break;
                case SILENT_FIELD:
                    this.silent = data.containsKey(key) && data.getString(key).equals("true");
                    break;
                case LINK_FIELD:
                    this.link = data.getString(key);
                    break;
                case ID_FIELD:
                    this.id = Integer.parseInt(data.getString(key));
                    break;
                case UNINSTALL_TRACKER_FIELD:
                    this.uninstallTracker = data.containsKey(key) && data.getString(key).equals("true");
                    break;
                case TEST_FIELD:
                    this.test = data.containsKey(key) && data.getString(key).equals("true");
                    break;
                case TITLE_FIELD:
                    this.title = data.getString(key);
                    break;
                case BODY_FIELD:
                    this.body = data.getString(key);
                    break;
                case CATEGORY_FIELD:
                    this.category = data.getString(key);
                    break;
                case LG_NOTIF_FOLDER_FIELD:
                    this.lgNotifFolder = data.getString(key);
                    break;
                case LG_NOTIF_IMG_FIELD:
                    this.lgNotifImage = data.getString(key);
                    break;
                case SM_NOTIF_FOLDER_FIELD:
                    this.smNotifFolder = data.getString(key);
                    break;
                case SM_NOTIF_IMG_FIELD:
                    this.smNotifImage = data.getString(key);
                    break;
                case SOUND_SILENT:
                    this.soundSilent = data.containsKey(key) && data.getString(key).equals("true");
                    break;
                case SOUND_DEFAULT:
                    this.soundDefault = data.containsKey(key) && data.getString(key).equals("true");
                    break;
                case SOUND_FILE_FIELD:
                    this.soundFile = data.getString(key);
                    break;
                case SOUND_FOLDER_FIELD:
                    this.soundFolder = data.getString(key);
                    break;
                case PAYLOAD_FIELD:
                    try {
                        this.payload = new JSONObject(data.getString(key));
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception processing notification payload.", e);
                    }
                    break;
            }
        }
    }

    public boolean isSilent() {
        return silent;
    }

    public boolean isUninstallTracker() {
        return uninstallTracker;
    }

    public boolean isTestMessage() {
        return test;
    }

    public int getId() {
        return id;
    }

    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Get the deeplink sent with the notification if any.
     *
     * @return the URL or null
     */
    public String getDeeplink() {
        return link;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getCategory() {
        return category;
    }

    public JSONObject getPayload() {
        return payload;
    }

    public String getLgNotifFolder() { return lgNotifFolder; }

    public String getLgNotifImage() { return lgNotifImage; }

    public String getSmNotifFolder() { return smNotifFolder; }

    public String getSmNotifImage() { return smNotifImage; }

    public Boolean getSoundSilent() { return soundSilent; }

    public Boolean getSoundDefault() { return soundDefault; }

    public String getSoundFile() { return soundFile; }

    public String getSoundFolder() { return soundFolder; }

    /**
     * Determine if the deeplink in the notification (if any) has been handled by the SDK or not.
     *
     * @return
     */
    public boolean linkHasBeenHandled() {
        return linkHandled;
    }

    public void setLinkHandled() {
        this.linkHandled = true;
    }

    public void setMainActivityLaunched() {
        this.mainActivityLaunched = true;
    }

    /**
     * Determine if the SDK fell back to the main activity when the notification was opened or not.
     *
     * @return
     */
    public boolean wasMainActivityLaunched() {
        return mainActivityLaunched;
    }

    /**
     * Creates a {@link NotificationCompat.Builder} constructed with the customization provided by Outbound.
     * @param context
     * @return
     */
    public NotificationCompat.Builder createNotificationBuilder(Context context) {
        Resources resources = context.getResources();
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            // since we are using context.getPackageName() this should never happen.
            Log.e(TAG, "Tried to access app that doesn't exist.");
        }


        int icon = appInfo.icon;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;


        NotificationCompat.Builder builder;
        String notificationChannelId = OutboundClient.getInstance().getNotificationChannelId();
        if (notificationChannelId != null) {
            builder = new NotificationCompat.Builder(context, notificationChannelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) && (notificationChannelId == null)) {
            // Warning: Notifications may not be delivered for Android 8+ (Oreo+) clients
            // Developers should provide a notification channel id () : https://developer.android.com/guide/topics/ui/notifiers/notifications.html
            Log.w(TAG, "Did you forget to provide a notification channel id? Notifications may not be delivered on sdk26+");
        }

        builder
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(this.getBody() == null ? "" : this.getBody()))
                .setContentText(this.getBody() == null ? "" : this.getBody())
                .setContentTitle(this.getTitle() == null ? "" : this.getTitle())
                .setSmallIcon(icon);

        try {
            String image = this.getSmNotifImage();
            String folder = this.getSmNotifFolder();
            if (folder != "" && image != "") {
                int resId = resources.getIdentifier(image, folder, context.getPackageName());
                if (resId != 0) {
                    builder.setSmallIcon(resId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Small icon doesn't exist");
            builder.setSmallIcon(icon);
        }

        try {
            String image = this.getLgNotifImage();
            String folder = this.getLgNotifFolder();
            Resources rs = pm.getResourcesForApplication(context.getPackageName());
            if (folder != "" && image != "") {
                int resId = resources.getIdentifier(image, folder, context.getPackageName());
                if (resId != 0) {
                    builder.setLargeIcon(BitmapFactory.decodeResource(rs, resId));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Large icon doesn't exist");
        }

        try {
            Uri media = null;
            if (this.getSoundSilent().equals(true)){
            } else if (this.getSoundDefault().equals(true)) {
                media = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            } else {
                int resId = resources.getIdentifier(this.getSoundFile(), this.getSoundFolder(), context.getPackageName());
                if (resId != 0) {
                    media = Uri.parse("android.resource://" + context.getPackageName() + "/" + resId);
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

        Intent intentToOpen = new Intent(context.getPackageName() + OutboundService.ACTION_OPEN_NOTIF);
        intentToOpen.setPackage(context.getPackageName());
        intentToOpen.putExtra(OutboundService.EXTRA_NOTIFICATION, this);

        PendingIntent pIntent = PendingIntent.getService(context, 0, intentToOpen, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pIntent);

        if (this.getCategory() != null) {
            builder.setCategory(this.getCategory());
        }

        return builder;
    }
}
