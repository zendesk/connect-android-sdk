package com.zendesk.connect.sampleapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.outbound.sdk.Event
import io.outbound.sdk.Outbound
import io.outbound.sdk.User
import kotlinx.android.synthetic.main.activity_main.*

const val LOG_TAG = "SampleMainActivity"

const val NOTIFICATION_CHANNEL_ID = "outbound_sample_app_notifications"
const val NOTIFICATION_CHANNEL_NAME = "Outbound Sample App Notifications"

// Place your configuration keys in ~/.gradle/gradle.properties
const val CONNECT_API_KEY: String = BuildConfig.CONNECT_API_KEY
const val GOOGLE_PROJECT_ID: String = BuildConfig.GOOGLE_PROJECT_ID

class MainActivity : AppCompatActivity() {

    private val sampleUser = User.Builder()
            .setUserId("th3_warth0g")
            .setFirstName("Frank")
            .setLastName("Reynolds")
            .build()

    private val sampleEvent = Event("Sample Event").apply {
        setUserId(sampleUser.userId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.enableVibration(true)

            val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

            Outbound.init(this.application, CONNECT_API_KEY,
                    GOOGLE_PROJECT_ID, NOTIFICATION_CHANNEL_ID)
        } else {
            Outbound.init(this.application, CONNECT_API_KEY, GOOGLE_PROJECT_ID)
        }

        identify_user_button.setOnClickListener {
            identifyUser(sampleUser)
        }

        track_event_button.setOnClickListener {
            trackEvent(sampleEvent)
        }

    }

    private fun identifyUser(user: User) {
        Log.d(LOG_TAG, "Identifying User")
        Outbound.identify(user)
    }

    private fun trackEvent(event: Event) {
        Log.d(LOG_TAG, "Tracking event")
        Outbound.track(event)
    }
}
