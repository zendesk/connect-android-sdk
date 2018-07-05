package com.zendesk.connect.sampleapp

import android.app.Notification
import android.graphics.Color
import android.util.Log
import io.outbound.sdk.OutboundMessagingService
import io.outbound.sdk.PushNotification

private const val LOG_TAG = "SampleMessagingService"

class SampleMessagingService: OutboundMessagingService() {

    /**
     * This method creates the notification to be displayed to the user from a push
     * notification. You can customise styling here based on the payload set in the
     * campaign.
     *
     * In this example the notification colour changes based on the value passed with
     * the key notificationColour to be red, green, or blue. If the key doesn't exist
     * or the value isn't supported then it will be the default colour.
     */
    override fun buildNotification(notification: PushNotification): Notification {
        val notificationBuilder = notification.createNotificationBuilder(this)

        if (notification.payload != null && notification.payload.has("notificationColour")) {
            when (notification.payload.getString("notificationColour").toLowerCase()) {
                "red" -> notificationBuilder.color = Color.RED
                "green" -> notificationBuilder.color = Color.GREEN
                "blue" -> notificationBuilder.color = Color.BLUE
                else -> Log.d(LOG_TAG, "Colour not supported, using default notification colour")
            }
        }

        return notificationBuilder.build()
    }
}