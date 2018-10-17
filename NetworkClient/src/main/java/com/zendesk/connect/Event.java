package com.zendesk.connect;

import java.util.*;
import com.zendesk.util.ObjectUtils;
/**
 * Event
 */
public class Event {
  private String userId = null;

  private long timestamp;

  private Object properties = null;

  private String event = null;

   /**
   * Get userId
   * @return userId
  **/
  public String getUserId() {
    return userId;
  }

  /**
   * Set userId
   * @param userId
  **/
  public void setUserId(String userId) {
    this.userId = userId;
  }

   /**
   * Get timestamp
   * @return timestamp
  **/
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Set timestamp
   * @param timestamp
  **/
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

   /**
   * Get properties
   * @return properties
  **/
  public Object getProperties() {
    return properties;
  }

  /**
   * Set properties
   * @param properties
  **/
  public void setProperties(Object properties) {
    this.properties = properties;
  }

   /**
   * Get event
   * @return event
  **/
  public String getEvent() {
    return event;
  }

  /**
   * Set event
   * @param event
  **/
  public void setEvent(String event) {
    this.event = event;
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
        ObjectUtils.equals(this.timestamp, event.timestamp) &&
        ObjectUtils.equals(this.properties, event.properties) &&
        ObjectUtils.equals(this.event, event.event);
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hash(userId, timestamp, properties, event);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Event {\n");
    
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
    sb.append("    event: ").append(toIndentedString(event)).append("\n");
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

