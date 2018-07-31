package com.zendesk.connect.testapp.helpers

import android.content.Context
import android.support.test.InstrumentationRegistry

/**
 * Clear any Outbound SDK data stored in SharedPrefs on the device
 */
fun clearSharedPrefs() {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val sharedPreferences = instrumentation.targetContext
            .getSharedPreferences("io.outbound.sdk.prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().clear().commit()
}

/**
 * Deletes the database created in OutboundStorage
 */
fun clearDatabase() {
    InstrumentationRegistry.getTargetContext().deleteDatabase("io.outbound.sdk")
}