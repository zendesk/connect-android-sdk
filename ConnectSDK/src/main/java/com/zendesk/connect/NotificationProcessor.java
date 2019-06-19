package com.zendesk.connect;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.zendesk.logger.Logger;
import com.zendesk.util.StringUtils;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Handles operations related to constructing and handling push notifications based on
 * data received via Firebase
 */
class NotificationProcessor {

    private static final String LOG_TAG = "NotificationProcessor";
    private static final String PAYLOAD_KEY = "payload";

    private Gson gson;
    private NotificationBuilder notificationBuilder;

    NotificationProcessor(Gson gson,
                          NotificationBuilder notificationBuilder) {
        this.gson = gson;
        this.notificationBuilder = notificationBuilder;
    }

    /**
     * Constructs a {@link Notification} from the {@link NotificationPayload} payload received
     *
     * @param data the {@link NotificationPayload} parsed from the push payload
     * @return a notification to be displayed, or null if an issue was encountered
     */
    Notification buildConnectNotification(NotificationPayload data) {
        if (data == null) {
            Logger.e(LOG_TAG, "Payload data was null, unable to create notification");
            return null;
        }

        String title = data.getTitle();
        String body = data.getBody();

        notificationBuilder.setTitle(StringUtils.ensureEmpty(title))
                .setBody(StringUtils.ensureEmpty(body))
                .setAutoCancel(true)
                .setLocalOnly(true);

        if (body != null) {
            NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
            style.bigText(body);
            notificationBuilder.setStyle(style);
        }

        String smallImageFile = data.getSmallNotificationImagePath();
        String smallImageFolder = data.getSmallNotificationFolderPath();
        notificationBuilder.setSmallIcon(smallImageFile,
                smallImageFolder,
                R.drawable.ic_connect_notification_icon);

        String largeImageFile = data.getLargeNotificationImagePath();
        String largeImageFolder = data.getLargeNotificationFolderPath();
        if (StringUtils.hasLengthMany(largeImageFile, largeImageFolder)) {
            notificationBuilder.setLargeIcon(largeImageFile, largeImageFolder);
        } else {
            Logger.w(LOG_TAG, "Large icon doesn't exist, there will be no large icon");
        }

        if (data.isSilent()) {
            notificationBuilder.setSilent();
        }

        String category = data.getCategory();
        if (category != null) {
            notificationBuilder.setCategory(category);
        }

        notificationBuilder.setPendingIntent(data);

        return notificationBuilder.build();
    }

    /**
     * Parses the body of a remoteMessage to a {@link NotificationPayload} for use in constructing
     * Connect notifications
     *
     * @param remoteMessage the {@link RemoteMessage} to be parsed
     * @return an instance of {@link NotificationPayload}
     */
    NotificationPayload parseRemoteMessage(RemoteMessage remoteMessage) {
        if (remoteMessage == null || remoteMessage.getData() == null) {
            Logger.e(LOG_TAG, "Notification data was null or empty");
            return null;
        }

        Map<String, String> data = remoteMessage.getData();
        JsonObject jsonObject = new JsonObject();

        // If the payload key is present, we extract it into a json tree so
        // it is treated as an object and not a string. This allows Gson to
        // parse it directly as a Map when creating the NotificationPayload
        String payloadString = data.get(PAYLOAD_KEY);
        if (payloadString != null) {
            Type payloadMapType = new TypeToken<Map<String, Object>>() { }.getType();
            Map<String, Object> map = gson.fromJson(payloadString, payloadMapType);
            jsonObject.add(PAYLOAD_KEY, gson.toJsonTree(map));
            data.remove(PAYLOAD_KEY);
        }

        // Add all of the remaining keys to the JsonObject as they are
        for (String key: data.keySet()) {
            jsonObject.addProperty(key, data.get(key));
        }

        return gson.fromJson(jsonObject, NotificationPayload.class);
    }
}
