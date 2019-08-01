package com.zendesk.connect;

/**
 * Describes how an In-Product Message can be dismissed by the user.
 */
enum IpmDismissType {

    /**
     * The IPM was dismissed by clicking outside the message content.
     */
    TAP_OUTSIDE,

    /**
     * The IPM was dismissed by sliding down the container view.
     */
    SLIDE_DOWN,

    /**
     * The IPM was dismissed because the user navigated back.
     */
    NAVIGATE_BACK
}
