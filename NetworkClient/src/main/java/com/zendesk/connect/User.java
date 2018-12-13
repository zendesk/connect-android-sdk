package com.zendesk.connect;

import java.util.*;

import com.google.gson.annotations.SerializedName;
import com.zendesk.util.ObjectUtils;

/**
 * User
 */
public class User {

    private final String userId;
    private final String previousId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phoneNumber;
    private final Map<String, Object> attributes;
    private final String groupId;
    private final Map<String, Object> groupAttributes;
    private final String timezone;
    private final List<String> fcm;
    private final List<String> apns;

    /**
     * @param userId 
     * @param previousId 
     * @param firstName 
     * @param lastName 
     * @param email 
     * @param phoneNumber 
     * @param attributes 
     * @param groupId 
     * @param groupAttributes 
     * @param timezone 
     * @param fcm
     * @param apns 
     */
    User(String userId, String previousId, String firstName, String lastName, String email, String phoneNumber, Map<String, Object> attributes, String groupId, Map<String, Object> groupAttributes, String timezone, List<String> fcm, List<String> apns) {
        this.userId = userId;
        this.previousId = previousId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.attributes = attributes;
        this.groupId = groupId;
        this.groupAttributes = groupAttributes;
        this.timezone = timezone;
        this.fcm = fcm;
        this.apns = apns;
    }

    /**
     * Get userId
     * @return userId
     */
    public String getUserId() {
      return userId;
    }
    
    /**
     * Get previousId
     * @return previousId
     */
    public String getPreviousId() {
      return previousId;
    }
    
    /**
     * Get firstName
     * @return firstName
     */
    public String getFirstName() {
      return firstName;
    }
    
    /**
     * Get lastName
     * @return lastName
     */
    public String getLastName() {
      return lastName;
    }
    
    /**
     * Get email
     * @return email
     */
    public String getEmail() {
      return email;
    }
    
    /**
     * Get phoneNumber
     * @return phoneNumber
     */
    public String getPhoneNumber() {
      return phoneNumber;
    }
    
    /**
     * Get attributes
     * @return attributes
     */
    public Map<String, Object> getAttributes() {
      return attributes;
    }
    
    /**
     * Get groupId
     * @return groupId
     */
    public String getGroupId() {
      return groupId;
    }
    
    /**
     * Get groupAttributes
     * @return groupAttributes
     */
    public Map<String, Object> getGroupAttributes() {
      return groupAttributes;
    }
    
    /**
     * Get timezone
     * @return timezone
     */
    public String getTimezone() {
      return timezone;
    }
    
    /**
     * Get fcm
     * @return fcm
     */
    public List<String> getFcm() {
      return fcm;
    }
    
    /**
     * Get apns
     * @return apns
     */
    public List<String> getApns() {
      return apns;
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
                ObjectUtils.equals(this.fcm, user.fcm) &&
                ObjectUtils.equals(this.apns, user.apns);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hash(userId, previousId, firstName, lastName, email, phoneNumber, attributes, groupId, groupAttributes, timezone, fcm, apns);
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
        sb.append("    apns: ").append(toIndentedString(apns)).append("\n");
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

