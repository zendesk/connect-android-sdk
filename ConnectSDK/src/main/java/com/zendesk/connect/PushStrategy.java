package com.zendesk.connect;

import java.util.Map;

/**
 * Defines a push notification processing strategy
 */
interface PushStrategy {

    /**
     * Perform any processing appropriate for the given push data payload
     *
     * @param data the data payload from a received push notification
     */
    void process(Map<String, String> data);

}
