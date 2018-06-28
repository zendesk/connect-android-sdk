package io.outbound.sdk

import android.app.Application

fun initSdkForTesting(app: Application, apiKey: String, gcmSenderId: String,
                      notificationChannelId: String?, testUrl: String) {
    Outbound.initForTesting(app, apiKey, gcmSenderId, notificationChannelId, testUrl)
}