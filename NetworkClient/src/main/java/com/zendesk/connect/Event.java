package com.zendesk.connect;

import java.util.*;
import com.zendesk.util.ObjectUtils;

/**
 * Event
 */
public class Event {

    private final String userId;
    private final String event;
    private final Map<String, Object> properties;
    private final long timestamp;

    /**
     * @param userId 
     * @param event 
     * @param properties 
     * @param timestamp 
     */
    Event(String userId, String event, Map<String, Object> properties, long timestamp) {
        this.userId = userId;
        this.event = event;
        this.properties = properties;
        this.timestamp = timestamp;
    }

    /**
     * Get userId
     * @return userId
     */
    public String getUserId() {
      return userId;
    }
    
    /**
     * Get event
     * @return event
     */
    public String getEvent() {
      return event;
    }
    
    /**
     * Get properties
     * @return properties
     */
    public Map<String, Object> getProperties() {
      return properties;
    }
    
    /**
     * Get timestamp
     * @return timestamp
     */
    public long getTimestamp() {
      return timestamp;
    }
    
    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Event event = (Event) o;
        return ObjectUtils.equals(this.userId, event.userId) &&
                ObjectUtils.equals(this.event, event.event) &&
                ObjectUtils.equals(this.properties, event.properties) &&
                ObjectUtils.equals(this.timestamp, event.timestamp);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hash(userId, event, properties, timestamp);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Event {\n");
        
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    event: ").append(toIndentedString(event)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
        sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
          return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}

