package io.outbound.sdk;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;

import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Model of an Outbound user.
 */
public class User {
    @Expose private final String userId;
    @Expose private final String anonId;
    @Expose private final String firstName;
    @Expose private final String lastName;
    @Expose private final String email;
    @Expose private final String phoneNumber;
    @Expose private final Map<String, Object> attributes;
    @Expose private final Map<String, Object> groupAttributes;
    @Expose private String[] gcm;
    @Expose private String groupId;
    @Expose private String previousId;
    @Expose private String timezone;

    private boolean anonymous;

    private User(@Nullable String userId, @Nullable String anonId,
                 @Nullable String firstName, @Nullable String lastName,
                 @Nullable String email, @Nullable String phoneNumber,
                 @Nullable String timezone,
                 @Nullable Map<String, Object> attributes,
                 @Nullable String groupId, @Nullable Map<String, Object> groupAttributes,
                 @Nullable boolean anonymous) {

        if (userId != null && userId.isEmpty()) {
            throw new IllegalArgumentException("The passed id is null or empty.  User's must have an id.");
        }

        this.userId = userId;
        this.anonId = anonId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.attributes = attributes;
        this.groupId = groupId;
        this.groupAttributes = groupAttributes;

        if (timezone == null) {
            timezone = TimeZone.getDefault().getID();
        }
        this.timezone = timezone;

        this.anonymous = anonymous;
    }

    /**
     * Determine if the user is anonymous or explicitly identified.
     * @return
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * Get the user's ID.
     *
     * @return
     */
    public String getUserId() {
        if (isAnonymous()) {
            return anonId;
        }
        return userId;
    }

    /**
     * Get the user's first name if set. Could return null.
     *
     * @return
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Get the user's last name if set. Could return null.
     *
     * @return
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Get the user's email address if set. Could return null.
     * @return
     */
    public String getEmail() {
        return email;
    }

    /**
     * Get the user's GCM token if set. Could return null.
     *
     * <p>GCM token is only added to the user after they have been identified. This is not a shortcut
     * for retrieving the token from GCM.</p>
     *
     * @return
     */
    public String getGcmToken() {
        if (gcm == null) {
            return null;
        }
        return gcm[0];
    }

    /**
     * Set the user's GCM token.
     *
     * @param gcm
     */
    public void setGcmToken(String gcm) {
        if (this.gcm == null) {
            this.gcm = new String[1];
        }
        this.gcm[0] = gcm;
    }

    /**
     * Check if the user has a GCM token set.
     *
     * @return
     */
    public boolean hasGcmToken() {
        return gcm != null && gcm.length > 0 && gcm[0] != null;
    }

    /**
     * Get the user's phone number if set. Could return null.
     *
     * <p>This method does <b>NOT</b> get the device's phone number. A phone number must be set
     * when creating a user.</p>
     *
     * @return
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Get the user's attributes. Could possibly return null or an empty Map.
     *
     * <p>Attributes do not include any top level attributes such as email, phone number, first and
     * last name or GCM token.</p>
     *
     * @return
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Get the user's group number if they belong to a group. Could return null.
     *
     * @return
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Get any group attributes associated with the user if any are set. Could return null or an empty
     * Map.
     *
     * @return
     */
    public Map<String, Object> getGroupAttributes() {
        return groupAttributes;
    }

    /**
     * When identifying a user, if an anonymous user was previously created, we need to alias those 2
     * users. by setting the previous id of a user to that of the anonymous user the 2 users become
     * the same user.
     *
     * @param prevId
     */
    public void setPrevioudId(String prevId) {
        this.anonymous = false;
        this.previousId = prevId;
    }

    /**
     * Get the user's previous id if they were aliased. Could return null.
     *
     * @return
     */
    public String getPreviousId() {
        return previousId;
    }

    /**
     * Constructs new Outbound Users.
     */
    public static class Builder {

        private String id, firstName, lastName, email, phoneNumber, groupId, timezone;
        private Map<String, Object> attributes, groupAttributes;
        private boolean isAnonymous;

        public Builder() {
        }

        public Builder setUserId(String id) {
            this.id = id;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder setTimezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder setGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder setGroupAttributes(Map<String, Object> groupAttributes) {
            this.groupAttributes = this.groupAttributes;
            return this;
        }

        public Builder setAnonymous() {
            this.isAnonymous = true;
            return this;
        }

        public User build() {
            if (id == null) {
                throw new IllegalStateException("User must have an ID.");
            }

            String uid = null;
            String anonId = null;
            if (isAnonymous) {
                anonId = id;
            } else {
                uid = id;
            }
            return new User(uid, anonId, firstName, lastName, email, phoneNumber, timezone, attributes, groupId,
                    groupAttributes, isAnonymous);
        }
    }

    public static User newAnonymousUser() {
        String userId = UUID.randomUUID().toString();
        return new User.Builder().setUserId(userId).setAnonymous().build();
    }
}