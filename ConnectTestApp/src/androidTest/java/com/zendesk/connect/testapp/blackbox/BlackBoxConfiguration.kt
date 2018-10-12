package com.zendesk.connect.testapp.blackbox

import android.app.Application
import android.support.test.rule.ActivityTestRule
import com.zendesk.connect.testapp.MainActivity
import com.zendesk.connect.testapp.helpers.TestInterceptor
import io.appflate.restmock.RESTMockServer
import io.outbound.sdk.Event
import io.outbound.sdk.User
import okhttp3.OkHttpClient

const val configPath = "/i/config/sdk/android"
const val identifyPath = "/v2/identify"
const val trackPath = "/v2/track"
const val registerPath = "/v2/gcm/register"
const val disablePath = "/v2/gcm/disable"
const val pairPath = "/i/testsend/push/pair/android"

val testClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(TestInterceptor(RESTMockServer.getUrl()))
        .build()

val testUser: User = User.newAnonymousUser()

val testEvent: Event = Event("Test Event")

val testApplication: Application =
    ActivityTestRule(MainActivity::class.java, true, true).apply {
        launchActivity(null)
    }.activity.application
