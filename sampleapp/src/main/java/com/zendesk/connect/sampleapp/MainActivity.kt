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

    private val sampleEvent = Event("Sample Event").apply {
        setUserId(sampleUser.userId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
