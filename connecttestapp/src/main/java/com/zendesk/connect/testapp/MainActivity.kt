package com.zendesk.connect.testapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.outbound.sdk.Event
import io.outbound.sdk.Outbound
import io.outbound.sdk.User
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var testUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init_sdk_button.setOnClickListener {
            Outbound.initForTesting(this.application, "rick", "morty", null, testUrl)
        }

        identify_user_button.setOnClickListener {
            val user = User.Builder()
                    .setUserId("testUser")
                    .setFirstName("Rick")
                    .setLastName("Sanchez")
                    .build()
            Outbound.identify(user)
        }

        track_event_button.setOnClickListener {
            val event = Event("Test Event")
            Outbound.track(event)
        }

        register_button.setOnClickListener {
            Outbound.register()
        }

        disable_button.setOnClickListener {
            Outbound.disable()
        }

        logout_button.setOnClickListener {
            Outbound.logout()
        }

        fetch_token_button.setOnClickListener {
            Outbound.getActiveToken()
        }
    }
}
