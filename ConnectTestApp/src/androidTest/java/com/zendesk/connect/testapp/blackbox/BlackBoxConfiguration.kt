package com.zendesk.connect.testapp.blackbox

import com.zendesk.connect.Event
import com.zendesk.connect.EventFactory
import com.zendesk.connect.User
import com.zendesk.connect.UserBuilder
import okhttp3.OkHttpClient

const val configPath = "/i/config/sdk/android"
const val identifyPath = "/v2/identify"
const val trackPath = "/v2/track"
const val registerPath = "/v2/fcm/register"
const val disablePath = "/v2/fcm/disable"
const val pairPath = "/i/testsend/push/pair/android"

val testClient: OkHttpClient = OkHttpClient.Builder()
        .build()

val testUser: User = UserBuilder.anonymousUser()

val testEvent: Event = EventFactory.createEvent("Test Event")
