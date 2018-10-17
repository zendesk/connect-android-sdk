package com.zendesk.connect

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.zendesk.test.MockedSharedPreferences
import org.junit.Before
import org.junit.Test

class StorageControllerTests {

    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesStorage: SharedPreferencesStorage

    private val gson = Gson()

    private lateinit var storageController: StorageController

    @Before
    fun setup() {
        mockSharedPreferences = MockedSharedPreferences().getSharedPreferences()
        sharedPreferencesStorage = SharedPreferencesStorage(mockSharedPreferences, gson)

        storageController = StorageController(sharedPreferencesStorage)
    }

    @Test
    fun `requesting storage from storage controller should return an instance of base storage`() {
        assertThat(storageController.storage()).isInstanceOf(BaseStorage::class.java)
    }

}