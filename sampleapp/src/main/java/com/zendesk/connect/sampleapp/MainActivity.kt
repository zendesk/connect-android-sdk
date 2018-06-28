package com.zendesk.connect.sampleapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.outbound.sdk.Event
import io.outbound.sdk.Outbound
import io.outbound.sdk.User
import kotlinx.android.synthetic.main.activity_main.*

private const val LOG_TAG = "SampleMainActivity"

class MainActivity : AppCompatActivity() {

    private val sampleUser = User.Builder()
            .setUserId("th3_warth0g")
            .setFirstName("Frank")
            .setLastName("Reynolds")
            .build()

    private val sampleEvent = Event("Sample Event")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        identify_user_button.setOnClickListener {
            identifyUser(sampleUser)
        }

        track_event_button.setOnClickListener {
            trackEvent(sampleEvent)
        }

        register_button.setOnClickListener {
            registerForPush()
        }

        disable_button.setOnClickListener {
            disablePushNotifications()
        }

        logout_button.setOnClickListener {
            logout()
        }

    }

    /**
     * Identifies a user within Outbound. Should be used when a user logs in or their
     * identifying information is updated.
     *
     * @param user: The user to be identified
     */
    private fun identifyUser(user: User) {
        Log.d(LOG_TAG, "Identifying User")
        Outbound.identify(user)
    }

    /**
     * Tracks the given event
     *
     * @param event: The event to be tracked
     */
    private fun trackEvent(event: Event) {
        Log.d(LOG_TAG, "Tracking event")
        Outbound.track(event)
    }

    /**
     * Registers the identified user for push notifications. If a user hasn't been identified yet,
     * then an anonymous user will be identified and registered for push.
     */
    private fun registerForPush() {
        Log.d(LOG_TAG, "Registering user for push")
        Outbound.register()
    }

    /**
     * Disables push notifications for the identified user
     */
    private fun disablePushNotifications() {
        Log.d(LOG_TAG, "Disabling push notifications for user")
        Outbound.disable()
    }

    /**
     * Logs the currently identified user out of Outbound by disabling the FCM
     * token and clearing information from storage.
     */
    private fun logout() {
        Log.d(LOG_TAG, "Logging out user")
        Outbound.logout()
    }
}
