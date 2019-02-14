package com.zendesk.connect

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.zendesk.test.MockedSharedPreferences
import com.zendesk.util.DigestUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class StorageControllerTests {

    private val PREFERENCES_KEY_CONFIG = "connect_preferences_key_config"
    private val PREFERENCES_KEY_USER = "connect_preferences_key_user"
    private val PREFERENCES_KEY_PRIVATE_KEY = "connect_preferences_key_private_key"

    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var spySharedPreferencesStorage: SharedPreferencesStorage

    private val gson = Gson()

    private val testConfig = Config(true, null)
    private val testUser = UserBuilder("some_id").build()
    private val testPrivateKey = "some_private_key"

    private lateinit var storageController: StorageController

    @Before
    fun setup() {
        mockSharedPreferences = MockedSharedPreferences().getSharedPreferences()
        spySharedPreferencesStorage = spy(SharedPreferencesStorage(mockSharedPreferences, gson))

        storageController = StorageController(spySharedPreferencesStorage)
    }

    @Test
    fun `requesting storage from storage controller should return an instance of base storage`() {
        assertThat(storageController.storage()).isInstanceOf(BaseStorage::class.java)
    }

    @Test
    fun `saveConfig should persist the given config object`() {
        storageController.saveConfig(testConfig)

        verify(spySharedPreferencesStorage).put(PREFERENCES_KEY_CONFIG, testConfig)
    }

    @Test
    fun `getConfig should retrieve the stored config object`() {
        storageController.saveConfig(testConfig)

        val retrievedConfig = storageController.config

        assertThat(retrievedConfig).isEqualTo(testConfig)
    }

    @Test
    fun `clearConfig should remove the stored config object`() {
        storageController.saveConfig(testConfig)

        storageController.clearConfig()

        assertThat(storageController.config).isNull()
    }

    @Test
    fun `saveUser should persist the given user object`() {
        storageController.saveUser(testUser)

        verify(spySharedPreferencesStorage).put(PREFERENCES_KEY_USER, testUser)
    }

    @Test
    fun `getUser should retrieve the stored user object`() {
        storageController.saveUser(testUser)

        val retrievedUser = storageController.user

        assertThat(retrievedUser).isEqualTo(testUser)
    }

    @Test
    fun `clearUser should remove the stored user object`() {
        storageController.saveUser(testUser)

        storageController.clearUser()

        assertThat(storageController.user).isNull()
    }

    @Test
    fun `savePrivateKey should persist the given private key with SHA256 encryption`() {
        storageController.savePrivateKey(testPrivateKey)

        val hashedKey = DigestUtils.sha256(testPrivateKey)

        verify(spySharedPreferencesStorage).put(PREFERENCES_KEY_PRIVATE_KEY, hashedKey)
    }

    @Test
    fun `isNewPrivateKey should return false if the provided key matches the stored key`() {
        storageController.savePrivateKey(testPrivateKey)

        assertThat(storageController.isNewPrivateKey(testPrivateKey)).isFalse()
    }

    @Test
    fun `isNewPrivateKey should return true if the provided key doesn't match the stored key`() {
        storageController.savePrivateKey(testPrivateKey)

        val testDevKey = "some_development_private_key"

        assertThat(storageController.isNewPrivateKey(testDevKey)).isTrue()
    }

    @Test
    fun `isNewPrivateKey should return true if there is no stored key`() {
        storageController.clearPrivateKey()

        assertThat(storageController.isNewPrivateKey(testPrivateKey)).isTrue()
    }

    @Test
    fun `clearPrivateKey should remove the stored private key`() {
        storageController.savePrivateKey(testPrivateKey)

        storageController.clearPrivateKey()

        assertThat(storageController.isNewPrivateKey(testPrivateKey)).isTrue()
    }

    @Test
    fun `clearAllStorage should remove everything from storage`() {
        storageController.saveConfig(testConfig)
        storageController.saveUser(testUser)
        storageController.savePrivateKey(testPrivateKey)

        storageController.clearAllStorage()

        assertThat(storageController.config).isNull()
        assertThat(storageController.user).isNull()
        assertThat(storageController.isNewPrivateKey(testPrivateKey)).isTrue()
    }

    @After
    fun tearDown() {

    }

}