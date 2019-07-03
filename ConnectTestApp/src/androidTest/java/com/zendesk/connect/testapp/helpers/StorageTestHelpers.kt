package com.zendesk.connect.testapp.helpers

import android.content.Context
import android.support.test.InstrumentationRegistry

/**
 * Clear any Connect SDK data stored in SharedPrefs on the device
 */
fun clearSharedPrefs() {
    InstrumentationRegistry.getTargetContext()
            .getSharedPreferences("connect_shared_preferences_storage", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
}

/**
 * Clears all queue related files created in the test app context
 */
fun clearFiles() {
    InstrumentationRegistry.getTargetContext().apply {
        deleteFile("connect_string_queue_file")
        deleteFile("connect_user_queue_file")
        deleteFile("connect_event_queue_file")
    }
}
