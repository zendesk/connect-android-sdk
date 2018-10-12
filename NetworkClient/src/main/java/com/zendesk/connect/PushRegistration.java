package com.zendesk.connect;

import com.zendesk.util.ObjectUtils;
/**
 * PushRegistration
 */
public class PushRegistration {
  private long timestamp;

  private String userId = null;

  private String token = null;

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
   * Get token
   * @return token
  **/
  public String getToken() {
    return token;
  }

  /**
   * Set token
   * @param token
  **/
  public void setToken(String token) {
    this.token = token;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PushRegistration pushRegistration = (PushRegistration) o;
    return ObjectUtils.equals(this.timestamp, pushRegistration.timestamp) &&
        ObjectUtils.equals(this.userId, pushRegistration.userId) &&
        ObjectUtils.equals(this.token, pushRegistration.token);
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hash(timestamp, userId, token);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Pushregistration {\n");
    
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    token: ").append(toIndentedString(token)).append("\n");
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

