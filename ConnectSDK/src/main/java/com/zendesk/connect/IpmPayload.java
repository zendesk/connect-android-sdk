package com.zendesk.connect;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

/**
 * Describes a basic Connect IPM payload that contains a heading, message, and a button with some action
 */
class IpmPayload implements Parcelable, Serializable {

    @SerializedName("_oid")
    private String instanceId;
    @SerializedName("ttl")
    private long timeToLive;

    @SerializedName("logo")
    private String logo;
    @SerializedName("heading")
    private String heading;
    @SerializedName("message")
    private String message;
    @SerializedName("buttonText")
    private String buttonText;
    @SerializedName("action")
    private String action;

    @SerializedName("headingFontColor")
    private String headingFontColor;
    @SerializedName("messageFontColor")
    private String messageFontColor;
    @SerializedName("backgroundColor")
    private String backgroundColor;
    @SerializedName("buttonBackgroundColor")
    private String buttonBackgroundColor;
    @SerializedName("buttonTextColor")
    private String buttonTextColor;

    /**
     * Constructs an instance of an {@link IpmPayload} model
     *
     * @param instanceId the unique identifier for this push payload
     * @param timeToLive the time to live duration
     * @param logo a reference to the source for the avatar logo
     * @param heading the heading text
     * @param message the message text
     * @param buttonText the button text
     * @param action the action for the button to perform
     * @param headingFontColor the heading font color
     * @param messageFontColor the message font color
     * @param backgroundColor the background color
     * @param buttonBackgroundColor the button background color
     * @param buttonTextColor the button text color
     */
    IpmPayload(String instanceId,
               long timeToLive,
               String logo,
               String heading,
               String message,
               String buttonText,
               String action,
               String headingFontColor,
               String messageFontColor,
               String backgroundColor,
               String buttonBackgroundColor,
               String buttonTextColor) {

        this.instanceId = instanceId;
        this.timeToLive = timeToLive;
        this.logo = logo;
        this.heading = heading;
        this.message = message;
        this.buttonText = buttonText;
        this.action = action;
        this.headingFontColor = headingFontColor;
        this.messageFontColor = messageFontColor;
        this.backgroundColor = backgroundColor;
        this.buttonBackgroundColor = buttonBackgroundColor;
        this.buttonTextColor = buttonTextColor;
    }

    private IpmPayload(Parcel in) {
        instanceId = in.readString();
        timeToLive = in.readLong();
        logo = in.readString();
        heading = in.readString();
        message = in.readString();
        buttonText = in.readString();
        action = in.readString();
        headingFontColor = in.readString();
        messageFontColor = in.readString();
        backgroundColor = in.readString();
        buttonBackgroundColor = in.readString();
        buttonTextColor = in.readString();
    }

    public static final Creator<IpmPayload> CREATOR = new Creator<IpmPayload>() {
        @Override
        public IpmPayload createFromParcel(Parcel in) {
            return new IpmPayload(in);
        }

        @Override
        public IpmPayload[] newArray(int size) {
            return new IpmPayload[size];
        }
    };

    /**
     * Gets the IPM unique instance id
     *
     * @return the instance id
     */
    String getInstanceId() {
        return instanceId;
    }

    /**
     * Gets the time to live for this IPM. It returns 0 if {@link #timeToLive} is not a valid long.
     *
     * @return the time to live, or 0 if {@link #timeToLive} is not a valid long
     */
    long getTimeToLive() {
        return timeToLive;
    }

    /**
     * Gets the logo for this IPM
     *
     * @return the logo
     */
    String getLogo() {
        return logo;
    }

    /**
     * Gets the heading text for this IPM
     *
     * @return the heading text
     */
    String getHeading() {
        return heading;
    }

    /**
     * Gets the message text for this IPM
     *
     * @return the message text
     */
    String getMessage() {
        return message;
    }

    /**
     * Gets the button text for this IPM
     *
     * @return the button text
     */
    String getButtonText() {
        return buttonText;
    }

    /**
     * Gets the action string for this IPM
     *
     * @return the action
     */
    String getAction() {
        return action;
    }

    /**
     * Gets the heading font color for this IPM
     *
     * @return the heading font color
     */
    String getHeadingFontColor() {
        return headingFontColor;
    }

    /**
     * Gets the message font color for this IPM
     *
     * @return the message font color
     */
    String getMessageFontColor() {
        return messageFontColor;
    }

    /**
     * Gets the background color for this IPM
     *
     * @return the background color
     */
    String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Gets the button background color for this IPM
     *
     * @return the button background color
     */
    String getButtonBackgroundColor() {
        return buttonBackgroundColor;
    }

    /**
     * Gets the button text color for this IPM
     *
     * @return the button text color
     */
    String getButtonTextColor() {
        return buttonTextColor;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(instanceId);
        parcel.writeLong(timeToLive);
        parcel.writeString(logo);
        parcel.writeString(heading);
        parcel.writeString(message);
        parcel.writeString(buttonText);
        parcel.writeString(action);
        parcel.writeString(headingFontColor);
        parcel.writeString(messageFontColor);
        parcel.writeString(backgroundColor);
        parcel.writeString(buttonBackgroundColor);
        parcel.writeString(buttonTextColor);
    }

    @NonNull
    @Override
    public String toString() {
        return "IpmPayload{" +
                "instanceId=" + instanceId +
                ", timeToLive=" + timeToLive +
                ", logo=" + logo +
                ", heading=" + heading +
                ", message=" + message +
                ", buttonText=" + buttonText +
                ", action=" + action +
                ", headingFontColor=" + headingFontColor +
                ", messageFontColor=" + messageFontColor +
                ", backgroundColor=" + backgroundColor +
                ", buttonBackgroundColor=" + buttonBackgroundColor +
                ", buttonTextColor=" + buttonTextColor +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IpmPayload that = (IpmPayload) o;
        return timeToLive == that.timeToLive
                && Objects.equals(instanceId, that.instanceId)
                && Objects.equals(logo, that.logo)
                && Objects.equals(heading, that.heading)
                && Objects.equals(message, that.message)
                && Objects.equals(buttonText, that.buttonText)
                && Objects.equals(action, that.action)
                && Objects.equals(headingFontColor, that.headingFontColor)
                && Objects.equals(messageFontColor, that.messageFontColor)
                && Objects.equals(backgroundColor, that.backgroundColor)
                && Objects.equals(buttonBackgroundColor, that.buttonBackgroundColor)
                && Objects.equals(buttonTextColor, that.buttonTextColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                instanceId,
                timeToLive,
                logo,
                heading,
                message,
                buttonText,
                action,
                headingFontColor,
                messageFontColor,
                backgroundColor,
                buttonBackgroundColor,
                buttonTextColor
        );
    }
}
