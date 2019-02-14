package com.zendesk.connect

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class ConnectTests {

    private val connect = Connect.INSTANCE

    private val testProdKey = "some_production_private_key"

    @Mock
    private lateinit var mockConnectComponent: ConnectComponent

    @Mock
    private lateinit var mockStorageController: StorageController

    @Mock
    private lateinit var mockUserQueue: ConnectQueue<User>

    @Mock
    private lateinit var mockEventQueue: ConnectQueue<Event>

    @Before
    fun setUp() {
        `when`(mockConnectComponent.storageController()).thenReturn(mockStorageController)
        `when`(mockConnectComponent.userQueue()).thenReturn(mockUserQueue)
        `when`(mockConnectComponent.eventQueue()).thenReturn(mockEventQueue)
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

    @After
    fun tearDown() {

    }
}