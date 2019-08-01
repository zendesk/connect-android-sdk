package com.zendesk.connect.sampleapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.squareup.leakcanary.LeakCanary
import com.zendesk.connect.Connect
import com.zendesk.logger.Logger

private const val LOG_TAG = "SampleApplication"

// Place your configuration keys in ~/.gradle/gradle.properties
private const val CONNECT_PRIVATE_KEY: String = BuildConfig.CONNECT_PRIVATE_KEY

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Logger.setLoggable(true)

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Logger.d(LOG_TAG, "Device running Oreo or above, creating notification channels")

            val notificationChannel = createConnectNotificationChannel()
            val silentNotificationChannel = createConnectSilentNotificationChannel()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(notificationChannel)
            notificationManager.createNotificationChannel(silentNotificationChannel)
        }

        Log.d(LOG_TAG, "Initialising Connect")
        Connect.INSTANCE.init(this, CONNECT_PRIVATE_KEY)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createConnectNotificationChannel(): NotificationChannel {
        val notificationChannelId = getString(R.string.connect_notification_channel_id)
        val notificationChannelName = getString(R.string.connect_notification_channel_name)

        val notificationChannel = NotificationChannel(
                notificationChannelId,
                notificationChannelName,
                NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationChannel.enableVibration(true)

        return notificationChannel
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createConnectSilentNotificationChannel(): NotificationChannel {
        val silentNotificationChannelId = getString(R.string.connect_silent_notification_channel_id)
        val silentNotificationChannelName = getString(R.string.connect_silent_notification_channel_name)

        val notificationChannel = NotificationChannel(
                silentNotificationChannelId,
                silentNotificationChannelName,
                NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationChannel.setSound(null, null)

        return notificationChannel
    }
}
