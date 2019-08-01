package com.zendesk.connect.sampleapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.RemoteMessage
import com.zendesk.connect.Connect
import com.zendesk.connect.ConnectMessagingService
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

        alias_user_button.setOnClickListener {
            // Suffix the sample user id with "_alias" to use as the alias id
            aliasUser(sampleUser, "${sampleUser.userId}_alias")
        }

        track_event_button.setOnClickListener {
            trackEvent(EventFactory.createEvent("Sample Event"))
        }

        register_button.setOnClickListener {
            registerForPush()
        }

        send_ipm_button.setOnClickListener {
            sendIpm()
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
     * Aliases the sample user. Should be used when you want to link a new user id
     * with an existing user.
     *
     * @param user: The user to be aliased
     * @param aliasId: The new id for the user
     */
    private fun aliasUser(user: User, aliasId: String) {
        Log.d(LOG_TAG, "Aliasing user")

        val aliasedUser = UserBuilder.aliased(user, aliasId)
        Connect.INSTANCE.identifyUser(aliasedUser)
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
     * Sends an In-Product Message to this device
     */
    private fun sendIpm() {
        val connectLogo = "https://d2o3top45uowdm.cloudfront.net/" +
            "media/8CD47F53-7BB3-407A-BFE897A72BCDF5D6/166E0EDA-60DE-41ED-ABD1AB1D5EAEE4C7/" +
            "webimage-5046F867-4EA4-4D8F-840EC2340BB97886.png"

        val ipmData = mapOf(
            "_oid" to "1",
            "type" to "ipm",
            "heading" to "Connect: In-App Messages",
            "headingFontColor" to "#000000",
            "message" to "In-App Messages let you display a message to your customers",
            "messageFontColor" to "#737373",
            "logo" to connectLogo,
            "buttonText" to "Ok",
            "buttonTextColor" to "#ffffff",
            "buttonBackgroundColor" to "#1f73b7",
            "backgroundColor" to "#ffffff",
            "ttl" to "60"
        )

        val remoteMessage = RemoteMessage.Builder("sample_ipm")
            .setData(ipmData)
            .build()

        Log.d(LOG_TAG, "Sending sample IPM")
        ConnectMessagingService().onMessageReceived(remoteMessage)
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
