package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ConnectNotificationTests {

    private val instanceIdKey = "_oid"
    private val testPushKey = "_otm"

    private var testData: MutableMap<String, String> = mutableMapOf()

    @Test
    fun `INSTANCE_ID get key should return the key expected for the instance id field`() {
        val key = ConnectNotification.Keys.INSTANCE_ID.key

        assertThat(key).isEqualTo(instanceIdKey)
    }

    @Test
    fun `TEST_PUSH get key should return the key expected for the test push field`() {
        val key = ConnectNotification.Keys.TEST_PUSH.key

        assertThat(key).isEqualTo(testPushKey)
    }

    @Test
    fun `is connect push should return false if given an invalid data payload`() {
        val result = ConnectNotification.isConnectPush(null)

        assertThat(result).isFalse()
    }

    @Test
    fun `is connect push should return false if payload doesn't contain an instance id`() {
        val result = ConnectNotification.isConnectPush(testData)

        assertThat(result).isFalse()
    }

    @Test
    fun `is connect push should return false if payload contains an empty instance id`() {
        testData[ConnectNotification.Keys.INSTANCE_ID.key] = ""

        val result = ConnectNotification.isConnectPush(testData)

        assertThat(result).isFalse()
    }

    @Test
    fun `is connect push should return true if payload contains an instance id`() {
        testData[ConnectNotification.Keys.INSTANCE_ID.key] = "some_instance_id"

        val result = ConnectNotification.isConnectPush(testData)

        assertThat(result).isTrue()
    }

    @Test
    fun `is connect push should return false if payload doesn't contain a test push key`() {
        val result = ConnectNotification.isConnectPush(testData)

        assertThat(result).isFalse()
    }

    @Test
    fun `is connect push should return false if payload test push key is empty`() {
        testData[ConnectNotification.Keys.TEST_PUSH.key] = ""

        val result = ConnectNotification.isConnectPush(testData)

        assertThat(result).isFalse()
    }

    @Test
    fun `is connect push should return false if payload test push key is false`() {
        testData[ConnectNotification.Keys.TEST_PUSH.key] = "false"

        val result = ConnectNotification.isConnectPush(testData)

        assertThat(result).isFalse()
    }

    @Test
    fun `is connect push should return true if payload test push key is true`() {
        testData[ConnectNotification.Keys.TEST_PUSH.key] = "true"

        val result = ConnectNotification.isConnectPush(testData)

        assertThat(result).isTrue()
    }
}

