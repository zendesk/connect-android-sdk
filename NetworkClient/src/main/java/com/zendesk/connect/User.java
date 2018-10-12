package com.zendesk.connect;

import com.google.gson.annotations.SerializedName;
import com.zendesk.util.ObjectUtils;

import java.util.Arrays;
import java.util.Map;
/**
 * User
 */
public class User {
  private String userId = null;
  private String previousId = null;
  private String firstName = null;
  private String lastName = null;
  private String email = null;
  private String phoneNumber = null;
  private Map<String, Object> attributes = null;
  private String groupId = null;
  private Map<String, Object> groupAttributes = null;
  private String timezone = null;

  @SerializedName("gcm")
  private String[] fcm = null;

  /**
   * Get userId
   * @return userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Set userId
   * @param userId
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Get previousId
   * @return previousId
   */
  public String getPreviousId() {
    return previousId;
  }

  /**
   * Set previousId
   * @param previousId
   */
  public void setPreviousId(String previousId) {
    this.previousId = previousId;
  }

  /**
   * Get firstName
   * @return firstName
  **/
  public String getFirstName() {
    return firstName;
  }

  /**
   * Set firstName
   * @param firstName
  **/
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

   /**
   * Get lastName
   * @return lastName
  **/
  public String getLastName() {
    return lastName;
  }

  /**
   * Set lastName
   * @param lastName
  **/
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

   /**
   * Get email
   * @return email
  **/
  public String getEmail() {
    return email;
  }

  /**
   * Set email
   * @param email
  **/
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Get phoneNumber
   * @return phoneNumber
   */
  public String getPhoneNumber() {
    return phoneNumber;
  }

  /**
   * Set phoneNumber
   * @param phoneNumber
   */
  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  /**
   * Get attributes
   * @return attributes
  **/
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  /**
   * Set attributes
   * @param attributes
  **/
  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  /**
   * Get groupId
   * @return groupId
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Set groupId
   * @param groupId
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Get groupAttributes
   * @return groupAttributes
   */
  public Map<String, Object> getGroupAttributes() {
    return groupAttributes;
  }

  /**
   * Set groupAttributes
   * @param groupAttributes
   */
  public void setGroupAttributes(Map<String, Object> groupAttributes) {
    this.groupAttributes = groupAttributes;
  }

  /**
   * Get timezone
   * @return timezone
   */
  public String getTimezone() {
    return timezone;
  }

  /**
   * Set timezone
   * @param timezone
   */
  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  /**
   * Get fcmToken
   * @return fcmToken
   */
  public String getFcmToken() {
    return fcm != null && fcm.length > 0 ? fcm[0] : null;
  }

  public void setFcmToken(String fcmToken) {
    this.fcm = new String[] { fcmToken };
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return ObjectUtils.equals(this.userId, user.userId) &&
        ObjectUtils.equals(this.previousId, user.previousId) &&
        ObjectUtils.equals(this.firstName, user.firstName) &&
        ObjectUtils.equals(this.lastName, user.lastName) &&
        ObjectUtils.equals(this.email, user.email) &&
        ObjectUtils.equals(this.phoneNumber, user.phoneNumber) &&
        ObjectUtils.equals(this.attributes, user.attributes) &&
        ObjectUtils.equals(this.groupId, user.groupId) &&
        ObjectUtils.equals(this.groupAttributes, user.groupAttributes) &&
        ObjectUtils.equals(this.timezone, user.timezone) &&
        Arrays.equals(this.fcm, user.fcm);
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hash(userId, previousId, firstName, lastName, email, phoneNumber, attributes, groupId, groupAttributes, timezone, fcm);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class User {\n");

    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    previousId: ").append(toIndentedString(previousId)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    phoneNumber: ").append(toIndentedString(phoneNumber)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
    sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
    sb.append("    groupAttributes: ").append(toIndentedString(groupAttributes)).append("\n");
    sb.append("    timezone: ").append(toIndentedString(timezone)).append("\n");
    sb.append("    fcm: ").append(toIndentedString(fcm)).append("\n");
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

