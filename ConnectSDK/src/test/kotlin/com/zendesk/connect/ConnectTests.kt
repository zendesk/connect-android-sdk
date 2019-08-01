package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import com.zendesk.logger.Logger
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify

class ConnectTests {

    companion object {
        private const val NOT_INITIALIZED_LOG = "Connect SDK has not been initialised"
    }

    private val connect = Connect.INSTANCE
    private val testProdKey = "some_production_private_key"

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    private val mockConnectComponent = mock<ConnectComponent>()
    private val mockStorageController = mock<StorageController>()
    private val mockUserQueue = mock<ConnectQueue<User>>()
    private val mockEventQueue = mock<ConnectQueue<Event>>()
    private val mockConnectScheduler = mock<ConnectScheduler>()
    private val mockInstanceId = mock<ConnectInstanceId>()

    @Before
    fun setUp() {
        given(mockConnectComponent.storageController()).willReturn(mockStorageController)
        given(mockConnectComponent.userQueue()).willReturn(mockUserQueue)
        given(mockConnectComponent.eventQueue()).willReturn(mockEventQueue)
        given(mockConnectComponent.scheduler()).willReturn(mockConnectScheduler)
        given(mockConnectComponent.instanceId()).willReturn(mockInstanceId)
    }

    @After
    fun tearDown() {
        Connect.INSTANCE.reset()
    }

    @Test
    fun `init should call foregroundListener on the component`() {
        Connect.INSTANCE.init(mockConnectComponent)

        verify(mockConnectComponent).foregroundListener()
    }

    @Test
    fun `getComponent should return null if the SDK has not been initialised`() {
        assertThat(Connect.INSTANCE.component).isNull()
    }

    @Test
    fun `getComponent should log a warning if the SDK is not initialised`() {
        Connect.INSTANCE.component
        assertThat(logAppender.lastLog()).isEqualTo(NOT_INITIALIZED_LOG)
    }

    @Test
    fun `updateStoredPrivateKey should store the provided private key`() {
        connect.updateStoredPrivateKey(mockConnectComponent, testProdKey)

        verify(mockStorageController).savePrivateKey(testProdKey)
    }

    @Test
    fun `updateStoredPrivateKey should check if the provided key is a new key`() {
        connect.updateStoredPrivateKey(mockConnectComponent, testProdKey)

        verify(mockStorageController).isNewPrivateKey(testProdKey)
    }

    @Test
    fun `updateStoredPrivateKey should clear general storage if the new key is different to the stored key`() {
        `when`(mockStorageController.isNewPrivateKey(anyString())).thenReturn(true)

        connect.updateStoredPrivateKey(mockConnectComponent, testProdKey)

        verify(mockStorageController).clearAllStorage()
    }

    @Test
    fun `updateStoredPrivateKey should clear the user queue if the new key is different to the stored key`() {
        `when`(mockStorageController.isNewPrivateKey(anyString())).thenReturn(true)

        connect.updateStoredPrivateKey(mockConnectComponent, testProdKey)

        verify(mockUserQueue).clear()
    }

    @Test
    fun `updateStoredPrivateKey should clear the event queue if the new key is different to the stored key`() {
        `when`(mockStorageController.isNewPrivateKey(anyString())).thenReturn(true)

        connect.updateStoredPrivateKey(mockConnectComponent, testProdKey)

        verify(mockEventQueue).clear()
    }

    @Test
    fun `isInitialised should return false if the SDK has not been initialised`() {
        assertThat(Connect.INSTANCE.isInitialised).isFalse()
    }

    @Test
    fun `isEnabled should return false if the SDK has not been enabled`() {
        assertThat(Connect.INSTANCE.isEnabled).isFalse()
    }

    @Test
    fun `isEnabled should log a warning if the SDK is not initialised`() {
        Connect.INSTANCE.isEnabled
        assertThat(logAppender.lastLog()).isEqualTo(NOT_INITIALIZED_LOG)
    }

    @Test
    fun `getUser should return null if the SDK has not been initialised `() {
        assertThat(Connect.INSTANCE.user).isNull()
    }

    @Test
    fun `getUser should log a warning if the SDK is not initialised`() {
        Connect.INSTANCE.user
        assertThat(logAppender.lastLog()).isEqualTo(NOT_INITIALIZED_LOG)
    }

}
