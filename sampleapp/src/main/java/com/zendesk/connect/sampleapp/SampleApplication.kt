package com.zendesk.connect.sampleapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import io.outbound.sdk.Outbound

private const val LOG_TAG = "SampleApplication"

private const val NOTIFICATION_CHANNEL_ID = "outbound_sample_app_notifications"
private const val NOTIFICATION_CHANNEL_NAME = "Outbound Sample App Notifications"

// Place your configuration keys in ~/.gradle/gradle.properties
private const val CONNECT_API_KEY: String = BuildConfig.CONNECT_API_KEY
private const val GOOGLE_PROJECT_ID: String = BuildConfig.GOOGLE_PROJECT_ID

class SampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d(LOG_TAG, "Initialising Outbound")

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.enableVibration(true)

            val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

            Outbound.init(this, CONNECT_API_KEY,
                    GOOGLE_PROJECT_ID, NOTIFICATION_CHANNEL_ID)
        } else {
            Outbound.init(this, CONNECT_API_KEY, GOOGLE_PROJECT_ID)
        }
    }
}