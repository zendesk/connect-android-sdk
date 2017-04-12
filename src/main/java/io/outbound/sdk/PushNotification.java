package io.outbound.sdk;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;
import java.util.StringTokenizer;

/**
 * The PushNotification represents one notification send from Outbound to the device. A PushNotification
 * is passed into the overridable methods of {@link OutboundService} allowing you to implement your own
 * logic around notifications.
 */
public class PushNotification implements Parcelable {
    private final static String TAG = BuildConfig.APPLICATION_ID;

    private final static String SILENT_FIELD = "_oq";
    private final static String TRACKER_FIELD = "_ogp";
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

    private boolean tracker;
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
        data.putString(TRACKER_FIELD, tracker ? "true" : "false");
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
                case TRACKER_FIELD:
                    this.tracker = data.containsKey(key) && data.getString(key).equals("true");
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

    public boolean isTracker() {
        return tracker;
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
}
