package com.zendesk.connect;

/**
 * Contains factory methods for creating {@link PushRegistration} objects for push
 * register / unregister requests. An integrator should never have to use these methods.
 */
class PushRegistrationFactory {

    /**
     * Creates a {@link PushRegistration} object with a given userId and pushToken. Creates
     * a timestamp upon creation.
     *
     * @param userId the id of the user
     * @param pushToken the push token for the device
     * @return a constructed {@link PushRegistration} object
     */
    static PushRegistration create(String userId, String pushToken) {
        PushRegistration registration = new PushRegistration();
        registration.setUserId(userId);
        registration.setToken(pushToken);
        registration.setTimestamp(System.currentTimeMillis());
        return registration;
    }
}
