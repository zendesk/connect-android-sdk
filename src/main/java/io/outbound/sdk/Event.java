package io.outbound.sdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Model for an Outbound event.
 */
public class Event {
    @Expose private String userId;
    @Expose private String anonId;
    @Expose @SerializedName("event") private final String name;
    @Expose private final Map<String, Object> properties;
    @Expose private long timestamp;

    /**
     * Create an event without any properties
     *
     * @param name name of the event
     */
    public Event(@Nullable String name) {
        this(name, new HashMap<String, Object>());
    }

    /**
     * Create an Outbound event with properites
     *
     * @param name name of the event
     * @param properties properties relating to the event
     */
    public Event(@Nullable String name, @NonNull Map<String, Object> properties) {
        this.name = name;
        this.properties = properties;

        this.timestamp = System.currentTimeMillis() / 1000;
    }

    /**
     * Set the user id of the user who commmitted the event. This is set automatically by the SDK.
     *
     * @param userId
     */
    public void setUserId(String userId) {
        setUserId(userId, false);
    }

    /**
     * Set the user id of the user who commmitted the event. This is set automatically by the SDK.
     *
     * @param userId
     * @param isAnon
     */
    public void setUserId(String userId, boolean isAnon) {
        if (isAnon) {
            this.anonId = userId;
        } else {
            this.userId = userId;
        }
    }

    /**
     * Add a property to the event.
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * Set the timestamp of when the event occurred. This is set automatically in the constructor but
     * can be overridden using this method.
     *
     * @param timestamp unix timestamp of when the event occurred
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get the name of the event
     *
     * @return
     */
    public String getName() {
        return name;
    }
}