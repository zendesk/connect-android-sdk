package com.zendesk.connect;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.zendesk.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Model defining the structure of a Connect notification.
 */
public class NotificationPayload implements Parcelable {

    @SerializedName("_oq")
    private boolean isQuietPush;
    @SerializedName("_ogp")
    private boolean isUninstallTracker;
    @SerializedName("_otm")
    private boolean isTestPush;
    @SerializedName("_silent")
    private boolean isSilent;
    @SerializedName("_soundDefault")
    private boolean isDefaultSound;

    @SerializedName("_onid")
    private int notificationId;

    @SerializedName("_oid")
    private String instanceId;
    @SerializedName("_odl")
    private String deeplinkUrl;
    @SerializedName("title")
    private String title;
    @SerializedName("body")
    private String body;
    @SerializedName("category")
    private String category;
    @SerializedName("_lni")
    private String largeNotificationImagePath;
    @SerializedName("_lnf")
    private String largeNotificationFolderPath;
    @SerializedName("_sni")
    private String smallNotificationImagePath;
    @SerializedName("_snf")
    private String smallNotificationFolderPath;

    @SerializedName("payload")
    private Map<String, Object> payload;

    protected NotificationPayload(Parcel in) {
        isQuietPush = in.readByte() != 0;
        isUninstallTracker = in.readByte() != 0;
        isTestPush = in.readByte() != 0;
        isSilent = in.readByte() != 0;
        isDefaultSound = in.readByte() != 0;
        notificationId = in.readInt();
        instanceId = in.readString();
        deeplinkUrl = in.readString();
        title = in.readString();
        body = in.readString();
        category = in.readString();
        largeNotificationImagePath = in.readString();
        largeNotificationFolderPath = in.readString();
        smallNotificationImagePath = in.readString();
        smallNotificationFolderPath = in.readString();
        payload = new HashMap<>();
        in.readMap(payload, Object.class.getClassLoader());
    }

    public static final Creator<NotificationPayload> CREATOR = new Creator<NotificationPayload>() {
        @Override
        public NotificationPayload createFromParcel(Parcel in) {
            return new NotificationPayload(in);
        }

        @Override
        public NotificationPayload[] newArray(int size) {
            return new NotificationPayload[size];
        }
    };

    public boolean isQuietPush() {
        return isQuietPush;
    }

    public boolean isUninstallTracker() {
        return isUninstallTracker;
    }

    public boolean isTestPush() {
        return isTestPush;
    }

    public boolean isSilent() {
        return isSilent;
    }

    public boolean isDefaultSound() {
        return isDefaultSound;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getDeeplinkUrl() {
        return deeplinkUrl;
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

    public String getLargeNotificationImagePath() {
        return largeNotificationImagePath;
    }

    public String getLargeNotificationFolderPath() {
        return largeNotificationFolderPath;
    }

    public String getSmallNotificationImagePath() {
        return smallNotificationImagePath;
    }

    public String getSmallNotificationFolderPath() {
        return smallNotificationFolderPath;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "NotificationPayload{" +
                "isQuietPush=" + isQuietPush +
                ", isUninstallTracker=" + isUninstallTracker +
                ", isTestPush=" + isTestPush +
                ", isSilent=" + isSilent +
                ", isDefaultSound=" + isDefaultSound +
                ", notificationId=" + notificationId +
                ", instanceId='" + instanceId + '\'' +
                ", deeplinkUrl='" + deeplinkUrl + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", category='" + category + '\'' +
                ", largeNotificationImagePath='" + largeNotificationImagePath + '\'' +
                ", largeNotificationFolderPath='" + largeNotificationFolderPath + '\'' +
                ", smallNotificationImagePath='" + smallNotificationImagePath + '\'' +
                ", smallNotificationFolderPath='" + smallNotificationFolderPath + '\'' +
                ", payload=" + payload +
                '}';
    }

    /**
     * Checks whether the notification payload belongs to a Connect notification
     *
     * @return true if the notification is from Connect, false otherwise
     */
    boolean isConnectNotification() {
        return StringUtils.hasLength(instanceId);
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (isQuietPush ? 1 : 0));
        parcel.writeByte((byte) (isUninstallTracker ? 1 : 0));
        parcel.writeByte((byte) (isTestPush ? 1 : 0));
        parcel.writeByte((byte) (isSilent ? 1 : 0));
        parcel.writeByte((byte) (isDefaultSound ? 1 : 0));
        parcel.writeInt(notificationId);
        parcel.writeString(instanceId);
        parcel.writeString(deeplinkUrl);
        parcel.writeString(title);
        parcel.writeString(body);
        parcel.writeString(category);
        parcel.writeString(largeNotificationImagePath);
        parcel.writeString(largeNotificationFolderPath);
        parcel.writeString(smallNotificationImagePath);
        parcel.writeString(smallNotificationFolderPath);
        parcel.writeMap(payload);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotificationPayload payload1 = (NotificationPayload) o;

        if (isQuietPush != payload1.isQuietPush) return false;
        if (isUninstallTracker != payload1.isUninstallTracker) return false;
        if (isTestPush != payload1.isTestPush) return false;
        if (isSilent != payload1.isSilent) return false;
        if (isDefaultSound != payload1.isDefaultSound) return false;
        if (notificationId != payload1.notificationId) return false;
        if (instanceId != null ? !instanceId.equals(payload1.instanceId) : payload1.instanceId != null)
            return false;
        if (deeplinkUrl != null ? !deeplinkUrl.equals(payload1.deeplinkUrl) : payload1.deeplinkUrl != null)
            return false;
        if (title != null ? !title.equals(payload1.title) : payload1.title != null) return false;
        if (body != null ? !body.equals(payload1.body) : payload1.body != null) return false;
        if (category != null ? !category.equals(payload1.category) : payload1.category != null)
            return false;
        if (largeNotificationImagePath != null ? !largeNotificationImagePath.equals(payload1.largeNotificationImagePath) : payload1.largeNotificationImagePath != null)
            return false;
        if (largeNotificationFolderPath != null ? !largeNotificationFolderPath.equals(payload1.largeNotificationFolderPath) : payload1.largeNotificationFolderPath != null)
            return false;
        if (smallNotificationImagePath != null ? !smallNotificationImagePath.equals(payload1.smallNotificationImagePath) : payload1.smallNotificationImagePath != null)
            return false;
        if (smallNotificationFolderPath != null ? !smallNotificationFolderPath.equals(payload1.smallNotificationFolderPath) : payload1.smallNotificationFolderPath != null)
            return false;
        return payload != null ? payload.equals(payload1.payload) : payload1.payload == null;
    }

    @Override
    public int hashCode() {
        int result = (isQuietPush ? 1 : 0);
        result = 31 * result + (isUninstallTracker ? 1 : 0);
        result = 31 * result + (isTestPush ? 1 : 0);
        result = 31 * result + (isSilent ? 1 : 0);
        result = 31 * result + (isDefaultSound ? 1 : 0);
        result = 31 * result + notificationId;
        result = 31 * result + (instanceId != null ? instanceId.hashCode() : 0);
        result = 31 * result + (deeplinkUrl != null ? deeplinkUrl.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (largeNotificationImagePath != null ? largeNotificationImagePath.hashCode() : 0);
        result = 31 * result + (largeNotificationFolderPath != null ? largeNotificationFolderPath.hashCode() : 0);
        result = 31 * result + (smallNotificationImagePath != null ? smallNotificationImagePath.hashCode() : 0);
        result = 31 * result + (smallNotificationFolderPath != null ? smallNotificationFolderPath.hashCode() : 0);
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        return result;
    }
}
