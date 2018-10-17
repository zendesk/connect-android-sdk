package io.outbound.sdk

import android.app.Application
import okhttp3.OkHttpClient

/**
 * Initialises the [Outbound] allowing us to pass in a testing [OkHttpClient] to redirect
 * network requests to a mock server url.
 */
internal fun testInitOutbound(app: Application, apiKey: String,
                     notificationChannel: String, testClient: OkHttpClient) {
    Outbound.initForTesting(app, apiKey, notificationChannel, testClient)
}