package io.outbound.sdk

import android.app.Application
import okhttp3.OkHttpClient

fun initSdkForTesting(app: Application, apiKey: String,
                      notificationChannel: String, testClient: OkHttpClient) {
    Outbound.initForTesting(app, apiKey, notificationChannel, testClient)
}