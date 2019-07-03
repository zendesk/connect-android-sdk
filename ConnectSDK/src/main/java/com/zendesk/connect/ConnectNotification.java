package com.zendesk.connect;

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
         * The instance id is a unique identifier for a Connect push and is present in all
         * Connect push payloads
         */
        INSTANCE_ID("_oid"),

        /**
         * The test message field identifies a push message as a test message
         */
        TEST_PUSH("_otm");

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
     * Checks whether the given payload belongs to a Connect push notification
     *
     * @param payload the payload to examine
     * @return true if the payload is a Connect push, false otherwise
     */
    static boolean isConnectPush(Map<String, String> payload) {
        if (payload == null) {
            return false;
        }

        boolean isConnectPush = StringUtils.hasLength(payload.get(Keys.INSTANCE_ID.getKey()));
        boolean isTestPush = Boolean.valueOf(payload.get(Keys.TEST_PUSH.getKey()));

        return isConnectPush || isTestPush;
    }

}
