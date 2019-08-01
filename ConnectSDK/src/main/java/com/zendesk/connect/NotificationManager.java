package com.zendesk.connect;

import android.app.Notification;

import androidx.core.app.NotificationManagerCompat;

import javax.inject.Inject;

/**
 * Wrapper class to abstract away Android dependencies to help with unit testing
 */
@ConnectScope
class NotificationManager {

    private NotificationManagerCompat notificationManager;

    /**
     * Creates an instance of {@link NotificationManager}
     *
     * @param notificationManager an instance of {@link NotificationManagerCompat}
     */
    @Inject
    NotificationManager(NotificationManagerCompat notificationManager) {
        this.notificationManager = notificationManager;
    }

    /**
     * Check if notifications are enabled on this device
     *
     * @return true if notifications are enabled, false otherwise
     */
    boolean areNotificationsEnabled() {
        return notificationManager.areNotificationsEnabled();
    }

    /**
     * Display the given notification on the device
     *
     * @param notificationId the unique id for this notification
     * @param notification the {@link Notification} to display
     */
    void notify(int notificationId, Notification notification) {
        notificationManager.notify(notificationId, notification);
    }

}
