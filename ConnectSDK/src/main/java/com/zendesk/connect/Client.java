package com.zendesk.connect;

/**
 * Client allowing access to Connect SDK functionality
 */
public interface Client {

    /**
     * Identifies a Connect {@link User}
     *
     * @param user the user to be identified
     */
    void identifyUser(User user);

    /**
     * Track a user triggered {@link Event}. Track event is sent immediately.
     *
     * @param event The event to be tracked
     */
    void trackEvent(Event event);

    /**
     * Batches a user triggered {@link Event} to be sent later
     *
     * @param event The event to be tracked
     */
    void batchEvent(Event event);

    /**
     * Registers the currently active user for push notifications
     */
    void registerForPush();

    /**
     * Disables push notifications for the currently active user
     */
    void disablePush();

    /**
     * Logs the currently active user out of the Connect SDK by
     * clearing storage and disabling push.
     */
    void logout();

}
