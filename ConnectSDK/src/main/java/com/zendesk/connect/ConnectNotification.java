package com.zendesk.connect;

import android.app.Notification;

import androidx.annotation.NonNull;

import com.zendesk.util.StringUtils;

import java.util.Map;

/**
 * Container for Connect notification specific keys and values, and utilities for determining
 * the type of push payload received.
 */
final class ConnectNotification {

    /**
     * Describes some of the keys expected in a Connect push payload
     */
    enum Keys {

        /**
         * The payload field contains any custom mappings provided in the Connect campaign
         */
        PAYLOAD("payload"),

        /**
         * The instance id is a unique identifier for a Connect push and is present in all
         * Connect push payloads
         */
        INSTANCE_ID("_oid"),

        /**
         * The notification id is a unique identifier used to identify a payload when calling
         * {@link androidx.core.app.NotificationManagerCompat#notify(int, Notification)}
         */
        NOTIFICATION_ID("_onid"),

        /**
         * The title field is used as the Push Notification title
         */
        TITLE("title"),

        /**
         * The body field is used as the Push Notification body
         */
        BODY("body"),

        /**
         * The deep link field can contain an {@link java.net.URI} that will be launched when the
         * user interacts with the push notification.
         */
        DEEP_LINK("_odl"),

        /**
         * The test message field identifies a push message as a test message
         */
        TEST_PUSH("_otm"),

        /**
         * The type field describes what type of push payload we have received so we can
         * parse and process it appropriately
         */
        TYPE("type"),

        /**
         * The time to live for a given IPM in seconds
         */
        TTL("ttl");

        private String key;

        Keys(String key) {
            this.key = key;
        }

        /**
         * Gets the string representation of this key
         *
         * @return the string value of this key
         */
        String getKey() {
            return key;
        }
    }

    /**
     * Describes values expected in Connect push payload fields
     */
    enum Values {

        /**
         * The type value we expect for an In-Product Message push payload
         */
        IPM("ipm");

        private String value;

        Values(String value) {
            this.value = value;
        }

        /**
         * Gets the string representation of the selected value
         *
         * @return the string value of this value
         */
        String getValue() {
            return value;
        }
    }

    /**
     * Describes the types of push notifications we expect from Connect
     */
    enum Types {

        /**
         * An in product message with text and a button
         */
        IPM,

        /**
         * A system push notification to be displayed in the tray
         */
        SYSTEM_PUSH,

        /**
         * We have no idea what this payload was for
         */
        UNKNOWN
    }

    /**
     * Checks whether the given payload belongs to a Connect push notification
     *
     * @param payload the payload to examine
     * @return true if the payload is a Connect push, false otherwise
     */
    @SuppressWarnings("ConstantConditions")
    static boolean isConnectPush(@NonNull Map<String, String> payload) {
        if (payload == null) {
            return false;
        }

        boolean isConnectPush = StringUtils.hasLength(payload.get(Keys.INSTANCE_ID.getKey()));
        boolean isTestPush = Boolean.valueOf(payload.get(Keys.TEST_PUSH.getKey()));

        return isConnectPush || isTestPush;
    }

    /**
     * Checks whether the given payload belongs to a Connect basic IPM notification
     *
     * @param payload the payload to examine
     * @return true if the payload is a Connect basic IPM, false otherwise
     */
    @SuppressWarnings("ConstantConditions")
    static boolean isIpm(@NonNull Map<String, String> payload) {
        if (payload == null) {
            return false;
        }
        String type = payload.get(Keys.TYPE.getKey());
        return type != null && type.equals(Values.IPM.getValue());
    }

    /**
     * Determines the type of push notification contained in the given payload
     *
     * @param payload the payload to examine
     * @return the appropriate instance of {@link Types}, or {@link Types#UNKNOWN}
     *          if we don't recognise the payload
     */
    @SuppressWarnings("ConstantConditions")
    static Types getNotificationType(@NonNull Map<String, String> payload) {
        if (payload == null || !isConnectPush(payload)) {
            return Types.UNKNOWN;
        }

        if (isIpm(payload)) {
            return Types.IPM;
        }

        return Types.SYSTEM_PUSH;
    }

}
