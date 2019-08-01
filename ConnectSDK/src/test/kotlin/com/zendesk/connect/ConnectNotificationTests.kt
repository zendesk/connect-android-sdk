package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class ConnectNotificationTests {

    private val payloadKey = "payload"
    private val instanceIdKey = "_oid"
    private val notificationIdKey = "_onid"
    private val titleKey = "title"
    private val bodyKey = "body"
    private val deepLinkKey = "_odl"
    private val testPushKey = "_otm"
    private val typeKey = "type"
    private val ipmValue = "ipm"
    private val nullMap = NullableTypesAsNonNull<Map<String, String>>().nullObject

    private lateinit var testData: MutableMap<String, String>

    @Before
    fun setUp() {
        testData = mutableMapOf()
    }

    @Test
    fun `PAYLOAD get key should return the key expected for the payload field`() {
        val key = ConnectNotification.Keys.PAYLOAD.key

        assertThat(key).isEqualTo(payloadKey)
    }

    @Test
    fun `INSTANCE_ID get key should return the key expected for the instance id field`() {
        val key = ConnectNotification.Keys.INSTANCE_ID.key

        assertThat(key).isEqualTo(instanceIdKey)
    }

    @Test
    fun `NOTIFICATION_ID get key should return the key expected for the notification id field`() {
        val key = ConnectNotification.Keys.NOTIFICATION_ID.key

        assertThat(key).isEqualTo(notificationIdKey)
    }

    @Test
    fun `TITLE get key should return the key expected for the title field`() {
        val key = ConnectNotification.Keys.TITLE.key

        assertThat(key).isEqualTo(titleKey)
    }

    @Test
    fun `BODY get key should return the key expected for the body field`() {
        val key = ConnectNotification.Keys.BODY.key

        assertThat(key).isEqualTo(bodyKey)
    }

    @Test
    fun `DEEP_LINK get key should return the key expected for the deep link field`() {
        val key = ConnectNotification.Keys.DEEP_LINK.key

        assertThat(key).isEqualTo(deepLinkKey)
    }

    @Test
    fun `TEST_PUSH get key should return the key expected for the test push field`() {
        val key = ConnectNotification.Keys.TEST_PUSH.key

        assertThat(key).isEqualTo(testPushKey)
    }

    @Test
    fun `TYPE get key should return the key expected for the type field`() {
        val key = ConnectNotification.Keys.TYPE.key

        assertThat(key).isEqualTo(typeKey)
    }

    @Test
    fun `IPM get value should return the value expected for an ipm type payload`() {
        val value = ConnectNotification.Values.IPM.value

        assertThat(value).isEqualTo(ipmValue)
    }

    // region isConnectPush
    @Test
    fun `is connect push should return false if given an invalid data payload`() {
        val result = ConnectNotification.isConnectPush(nullMap)

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
    // endregion

    @Test
    fun `is ipm should return false if given a null data payload`() {
        val result = ConnectNotification.isIpm(nullMap)

        assertThat(result).isFalse()
    }

    @Test
    fun `is ipm should return false if payload doesn't contain a type`() {
        val result = ConnectNotification.isIpm(testData)

        assertThat(result).isFalse()
    }

    @Test
    fun `is ipm should return false if payload contains a non-ipm type`() {
        testData[ConnectNotification.Keys.TYPE.key] = "mega-seed"

        val result = ConnectNotification.isIpm(testData)

        assertThat(result).isFalse()
    }

    @Test
    fun `is ipm should return true if payload contains an ipm type`() {
        testData[ConnectNotification.Keys.TYPE.key] = ConnectNotification.Values.IPM.value

        val result = ConnectNotification.isIpm(testData)

        assertThat(result).isTrue()
    }

    @Test
    fun `get notification type should return unknown for a null payload`() {
        val result = ConnectNotification.getNotificationType(nullMap)

        assertThat(result).isEqualTo(ConnectNotification.Types.UNKNOWN)
    }

    @Test
    fun `get notification type should return unknown for a non connect payload`() {
        val result = ConnectNotification.getNotificationType(testData)

        assertThat(result).isEqualTo(ConnectNotification.Types.UNKNOWN)
    }

    @Test
    fun `get notification type should return ipm for an in product message payload`() {
        testData[ConnectNotification.Keys.INSTANCE_ID.key] = "some_instance_id"
        testData[ConnectNotification.Keys.TYPE.key] = ConnectNotification.Values.IPM.value

        val result = ConnectNotification.getNotificationType(testData)

        assertThat(result).isEqualTo(ConnectNotification.Types.IPM)
    }

    @Test
    fun `get notification type should return system push for a connect system push payload`() {
        testData[ConnectNotification.Keys.INSTANCE_ID.key] = "some_instance_id"

        val result = ConnectNotification.getNotificationType(testData)

        assertThat(result).isEqualTo(ConnectNotification.Types.SYSTEM_PUSH)
    }

}
