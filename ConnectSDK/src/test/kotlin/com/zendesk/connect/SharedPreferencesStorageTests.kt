package com.zendesk.connect

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.zendesk.test.MockedSharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class SharedPreferencesStorageTests {

    private val gson = Gson()
    private val testUser = UserBuilder("c137")
            .setFirstName("Rick")
            .build()

    private lateinit var storage: SharedPreferencesStorage
    private lateinit var mockSharedPreferences: SharedPreferences

    @Before
    fun setup() {
        mockSharedPreferences = MockedSharedPreferences().getSharedPreferences()
        storage = SharedPreferencesStorage(mockSharedPreferences, gson)
    }

    @Test
    fun `put non null string should persist that string`() {
        storage.put("foo", "bar")
        assertThat(storage.get("foo")).isEqualTo("bar")
    }

    @Test
    fun `put null string should remove that key from storage`() {
        storage.put("foo", "bar")
        storage.put("foo", null)
        assertThat(storage.get("foo")).isNotEqualTo("bar")
    }

    @Test
    fun `put non null object should persist that object`() {
        storage.put("foo", testUser)
        assertThat(storage.get("foo", User::class.java)).isEqualTo(testUser)
    }

    @Test
    fun `put null object should remove object from storage`() {
        storage.put("foo", testUser)
        storage.put("foo", null)
        assertThat(storage.get("foo", User::class.java)).isNotEqualTo(testUser)
    }

    @Test
    fun `get string should return the string from storage if the key exists`() {
        storage.put("foo", "bar")
        assertThat(storage.get("foo")).isEqualTo("bar")
    }

    @Test
    fun `get string should return null if the key doesn't exist`() {
        assertThat(storage.get("foo")).isNull()
    }

    @Test
    fun `get string should return a string representation of a stored object if the key exists`() {
        storage.put("foo", testUser)
        assertThat(storage.get("foo")).isEqualTo(gson.toJson(testUser))
    }

    @Test
    fun `get clazz should return the deserialised object from storage if the key exists`() {
        storage.put("foo", testUser)
        assertThat(storage.get("foo", User::class.java)).isEqualTo(testUser)
    }

    @Test
    fun `get clazz should return null if the key doesn't exist`() {
        assertThat(storage.get("foo", User::class.java)).isNull()
    }

    @Test
    fun `get clazz should return null if json string is invalid`() {
        storage.put("foo", "{I ain't no user object}")
        assertThat(storage.get("foo", User::class.java)).isNull()
    }

    @Test
    fun `remove should remove a key value pair from storage`() {
        storage.put("foo", "bar")
        assertThat(storage.get("foo")).isNotNull()
        storage.remove("foo")
        assertThat(storage.get("foo")).isNull()
    }

    @Test
    fun `calling remove on a key that doesn't exist should do nothing`() {
        storage.remove("foo")
        assertThat(storage.get("foo")).isNull()
    }

    @Test
    fun `clear should remove all keys from storage`() {
        storage.put("foo", "bar")
        storage.put("baz", testUser)
        assertThat(storage.get("foo")).isNotNull()
        assertThat(storage.get("baz")).isNotNull()

        storage.clear()
        assertThat(storage.get("foo")).isNull()
        assertThat(storage.get("baz")).isNull()
    }

}