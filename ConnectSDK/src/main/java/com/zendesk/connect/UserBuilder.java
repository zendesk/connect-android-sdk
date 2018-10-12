package com.zendesk.connect;

import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class UserBuilder {

    private String userId;
    private String previousId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String fcmToken;
    private Map<String, Object> userAttributes;
    private String groupId;
    private Map<String, Object> groupAttributes;
    private String timezone;

    public UserBuilder(String userId) {
        this.userId = userId;
    }

    /**
     * Sets the previous ID for the user
     *
     * @param previousId the previous ID for the user
     * @return the builder
     */
    public UserBuilder setPreviousId(String previousId) {
        this.previousId = previousId;
        return this;
    }

    /**
     * Sets the user's first name
     *
     * @param firstName the user's first name
     * @return the builder
     */
    public UserBuilder setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * Sets the user's last name
     *
     * @param lastName the user's last name
     * @return the builder
     */
    public UserBuilder setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * Sets the user's email
     *
     * @param email the user's email
     * @return the builder
     */
    public UserBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Sets the user's phone number
     *
     * @param phoneNumber the user's phone number
     * @return the builder
     */
    public UserBuilder setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    /**
     * Sets the user's fcm token
     *
     * @param fcmToken the user's fcm token
     * @return the builder
     */
    public UserBuilder setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
        return this;
    }

    /**
     * Sets the user's attributes
     *
     * @param userAttributes the user's attributes
     * @return the builder
     */
    public UserBuilder setUserAttributes(Map<String, Object> userAttributes) {
        this.userAttributes = userAttributes;
        return this;
    }

    /**
     * Sets the user's group ID
     *
     * @param groupId the user's group ID
     * @return the builder
     */
    public UserBuilder setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    /**
     * Sets the user's group attributes
     *
     * @param groupAttributes the user's group attributes
     * @return the builder
     */
    public UserBuilder setGroupAttributes(Map<String, Object> groupAttributes) {
        this.groupAttributes = groupAttributes;
        return this;
    }

    /**
     * Sets the user's timezone
     *
     * @param timezone the user's timezone
     * @return the builder
     */
    public UserBuilder setTimezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    /**
     * Constructs a {@link User} with the current Builder configuration
     *
     * @return a constructed {@link User} model
     */
    public User build() {
        User user = new User();
        user.setUserId(this.userId);
        user.setPreviousId(this.previousId);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setEmail(this.email);
        user.setPhoneNumber(this.phoneNumber);
        user.setAttributes(this.userAttributes);
        user.setGroupId(this.groupId);
        user.setGroupAttributes(this.groupAttributes);
        user.setTimezone(this.timezone != null ? this.timezone : TimeZone.getDefault().getID());
        user.setFcmToken(this.fcmToken);
        return user;
    }

    /**
     * Constructs an anonymous user with a random {@link #userId} and no
     * other identifying information
     *
     * @return an anonymous {@link User}
     */
    public static User anonymousUser() {
        return new UserBuilder(UUID.randomUUID().toString()).build();
    }
}
