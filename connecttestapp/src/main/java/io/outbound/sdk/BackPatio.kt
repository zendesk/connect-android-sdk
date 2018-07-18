package io.outbound.sdk

import android.app.Application

fun initSdkForTesting(app: Application, apiKey: String,
                      notificationChannelId: String?, testUrl: String) {
    Outbound.initForTesting(app, apiKey, notificationChannelId, testUrl)
}