package com.zendesk.connect;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.NotificationCompat;

/**
 * A wrapper for {@link NotificationCompat.Builder} that allows us to more easily test construction
 * of {@link Notification}s without needing to mock the compat builder directly. The internal
 * builder can be swapped out in future for a different implementation if needed.
 */
class NotificationBuilder {

    private static final int CONNECT_INTENT_REQUEST_CODE = 0;

    private NotificationCompat.Builder builder;
    private Context context;
    private Resources resources;

    NotificationBuilder(NotificationCompat.Builder builder,
                        Context context) {
        this.builder = builder;
        this.context = context;
        this.resources = context.getResources();
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
     * Sets the small icon for the notification
     *
     * @param fileName the name of the icon file
     * @param folderName the name of the folder where the file is located
     * @return the builder
     */
    NotificationBuilder setSmallIcon(String fileName, String folderName) {
        int smallIconId = resources.getIdentifier(fileName, folderName, context.getPackageName());
        setSmallIcon(smallIconId);
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
     * @param payload the {@link NotificationPayload} to be included as the intent extra
     * @return the builder
     */
    NotificationBuilder setPendingIntent(NotificationPayload payload) {
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
