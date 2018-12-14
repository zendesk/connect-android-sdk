package com.zendesk.connect.sampleapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.zendesk.connect.Connect
import com.zendesk.connect.Event
import com.zendesk.connect.EventFactory
import com.zendesk.connect.User
import com.zendesk.connect.UserBuilder
import kotlinx.android.synthetic.main.activity_main.*

private const val LOG_TAG = "SampleMainActivity"

class MainActivity : AppCompatActivity() {

    private val sampleUser = UserBuilder("th3_warth0g")
            .setFirstName("Frank")
            .setLastName("Reynolds")
            .setEmail("thewarthog@example.com")
            .setPhoneNumber("01-123-4567")
            .setUserAttributes(mapOf(Pair("Occupation", "Making money")))
            .setGroupId("Frank's Fluids")
            .setGroupAttributes(mapOf(Pair("Merch", "Egg"), Pair("Fluid", "Wolf Cola")))
            .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        identify_user_button.setOnClickListener {
            identifyUser(sampleUser)
        }

        track_event_button.setOnClickListener {
            trackEvent(EventFactory.createEvent("Sample Event"))
        }

        register_button.setOnClickListener {
            registerForPush()
        }

        disable_button.setOnClickListener {
            disablePushNotifications()
        }

        logout_button.setOnClickListener {
            logoutUser()
        }

    }

    /**
     * Identifies a user within Connect. Should be used when a user logs in or their
     * identifying information is updated.
     *
     * @param user: The user to be identified
     */
    private fun identifyUser(user: User) {
        Log.d(LOG_TAG, "Identifying User")
        Connect.INSTANCE.identifyUser(user)
    }

    /**
     * Tracks the given event
     *
     * @param event: The event to be tracked
     */
    private fun trackEvent(event: Event) {
        Log.d(LOG_TAG, "Tracking event")
        Connect.INSTANCE.trackEvent(event)
    }

    /**
     * Registers the identified user for push notifications. If a user hasn't been identified yet,
     * then an anonymous user will be identified and registered for push.
     */
    private fun registerForPush() {
        Log.d(LOG_TAG, "Registering user for push")
        Connect.INSTANCE.registerForPush()
    }

    /**
     * Disables push notifications for the identified user
     */
    private fun disablePushNotifications() {
        Log.d(LOG_TAG, "Disabling push notifications for user")
        Connect.INSTANCE.disablePush()
    }

    /**
     * Logs the currently identified user out of Connect by disabling the FCM
     * token and clearing information from storage.
     */
    private fun logoutUser() {
        Log.d(LOG_TAG, "Logging out user")
        Connect.INSTANCE.logoutUser()
    }
}
