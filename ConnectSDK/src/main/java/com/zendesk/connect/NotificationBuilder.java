package com.zendesk.connect;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.DrawableRes;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.NotificationCompat;

import com.zendesk.logger.Logger;
import com.zendesk.util.StringUtils;

import javax.inject.Inject;

/**
 * A wrapper for {@link NotificationCompat.Builder} that allows us to more easily test construction
 * of {@link Notification}s without needing to mock the compat builder directly. The internal
 * builder can be swapped out in future for a different implementation if needed.
 */
@ConnectScope
class NotificationBuilder {

    private static final String LOG_TAG = "NotificationBuilder";

    private static final int CONNECT_INTENT_REQUEST_CODE = 0;

    private final NotificationCompat.Builder builder;
    private final Context context;
    private final Resources resources;
    private final String connectSilentNotificationChannelId;

    @Inject
    NotificationBuilder(NotificationCompat.Builder builder,
                        Context context,
                        @ConnectSilentNotificationChannelQualifier String connectSilentNotificationChannelId) {
        this.builder = builder;
        this.context = context;
        this.resources = context.getResources();
        this.connectSilentNotificationChannelId = connectSilentNotificationChannelId;
    }

    /**
     * Sets the title of the notification
     *
     * @param title the title of the notification
     * @return the builder
     */
    NotificationBuilder setTitle(String title) {
        builder.setContentTitle(title);
        return this;
    }

    /**
     * Sets the body of the notification
     *
     * @param body the body of the notification
     * @return the builder
     */
    NotificationBuilder setBody(String body) {
        builder.setContentText(body);
        return this;
    }

    /**
     * Sets the style of the notification
     *
     * @param style the style of the notification
     * @return the builder
     */
    NotificationBuilder setStyle(NotificationCompat.Style style) {
        builder.setStyle(style);
        return this;
    }

    /**
     * Sets whether or not the notification should automatically close
     *
     * @param autoCancel whether or not the notification should go away automatically
     * @return the builder
     */
    NotificationBuilder setAutoCancel(boolean autoCancel) {
        builder.setAutoCancel(autoCancel);
        return this;
    }

    /**
     * Sets whether or not the notification can display across devices
     *
     * @param localOnly whether or not the notification can display across devices
     * @return the builder
     */
    NotificationBuilder setLocalOnly(boolean localOnly) {
        builder.setLocalOnly(localOnly);
        return this;
    }

    /**
     * Sets the small icon for the notification
     *
     * @param smallIconId the resource id for the icon
     * @return the builder
     */
    NotificationBuilder setSmallIcon(int smallIconId) {
        builder.setSmallIcon(smallIconId);
        return this;
    }

    /**
     * Sets the small icon for the notification if the file exists, otherwise uses the provided
     * fallbackSmallIconId
     *
     * @param fileName the name of the icon file
     * @param folderName the name of the folder where the file is located
     * @param fallbackSmallIconId the fallback resource id for the icon if the file doesn't exist
     * @return the builder
     */
    NotificationBuilder setSmallIcon(String fileName,
                                     String folderName,
                                     @DrawableRes int fallbackSmallIconId) {

        int smallIconId = resources.getIdentifier(
                StringUtils.ensureEmpty(fileName),
                StringUtils.ensureEmpty(folderName),
                context.getPackageName());

        if (smallIconId != 0) {
            setSmallIcon(smallIconId);
        } else {
            Logger.w(LOG_TAG, "Small icon doesn't exist, using default icon");
            setSmallIcon(fallbackSmallIconId);
        }

        return this;
    }

    /**
     * Sets the large icon for the notification
     *
     * @param largeIcon the {@link Bitmap} of the large icon
     * @return the builder
     */
    NotificationBuilder setLargeIcon(Bitmap largeIcon) {
        builder.setLargeIcon(largeIcon);
        return this;
    }

    /**
     * Sets the large icon for the notification
     *
     * @param fileName the name of the icon file
     * @param folderName the name of the folder where the file is located
     * @return the builder
     */
    NotificationBuilder setLargeIcon(String fileName, String folderName) {
        int largeIcon = resources.getIdentifier(fileName, folderName, context.getPackageName());
        setLargeIcon(decodeLargeIconResource(resources, largeIcon));
        return this;
    }

    /**
     * Decodes a bitmap resource using {@link BitmapFactory}. This is "VisibileForTesting" just
     * so I can spy on the object and prevent this method from being called since bitmap is an
     * Android class
     *
     * @param resources an instance of {@link Resources}
     * @param largeIcon the resource id
     * @return The decoded {@link Bitmap}
     */
    @VisibleForTesting
    Bitmap decodeLargeIconResource(Resources resources, int largeIcon) {
        return BitmapFactory.decodeResource(resources, largeIcon);
    }

    /**
     * Sets the sound of the notification to null and its channel id to
     * {@link NotificationBuilder#connectSilentNotificationChannelId}
     *
     * @return the builder
     */
    NotificationBuilder setSilent() {
        // Set sound as null for pre-Oreo compatibility
        builder.setSound(null);
        // Set the notification channel for post-Oreo
        builder.setChannelId(connectSilentNotificationChannelId);
        return this;
    }

    /**
     * Sets the category of the notification
     *
     * @param category the category of the notification
     * @return the builder
     */
    NotificationBuilder setCategory(String category) {
        builder.setCategory(category);
        return this;
    }

    /**
     * Sets the pending intent for the notification
     *
     * @param payload the {@link SystemPushPayload} to be included as the intent extra
     * @return the builder
     */
    NotificationBuilder setPendingIntent(SystemPushPayload payload) {
        String actionName = context.getPackageName() + ConnectActionService.ACTION_OPEN_NOTIFICATION;
        Intent intentToOpen = new Intent(actionName);
        intentToOpen.setPackage(context.getPackageName());
        intentToOpen.putExtra(ConnectActionService.EXTRA_NOTIFICATION, payload);

        PendingIntent pendingIntent = PendingIntent.getService(context,
                CONNECT_INTENT_REQUEST_CODE,
                intentToOpen,
                PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(pendingIntent);
        return this;
    }

    /**
     * Builds the notification
     *
     * @return the constructed {@link Notification}
     */
    Notification build() {
        return builder.build();
    }
}
