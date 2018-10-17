package com.zendesk.connect;

import java.util.Map;

/**
 * Simple factory methods for quickly creating {@link Event} objects
 */
public class EventFactory {

    /**
     * Create an event with properties
     *
     * @param eventName the name of the event
     * @param properties the properties of the event
     * @return a constructed {@link Event} object
     */
    public static Event createEvent(String eventName, Map<String, Object> properties) {
        Event event = new Event();
        event.setEvent(eventName);
        event.setProperties(properties);
        event.setTimestamp(System.currentTimeMillis());
        return event;
    }

    /**
     * Create an event with no properties
     *
     * @param eventName the name of the event
     * @return a constructed {@link Event} object
     */
    public static Event createEvent(String eventName) {
        return createEvent(eventName, null);
    }
}
