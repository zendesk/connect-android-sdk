package com.zendesk.connect

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.google.common.truth.Truth.assertThat
import com.zendesk.logger.Logger
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Call
import java.io.IOException
import java.io.Serializable

@RunWith(MockitoJUnitRunner.Silent::class)
class ConnectActionProcessorTests {

    private val INVALID_INTENT_WARNING = "Intent was null or contained invalid action name"
    private val NULL_PAYLOAD_WARNING = "Payload must not be null"
    private val INVALID_PAYLOAD_WARNING = "Payload is not a valid Connect notification"
    private val TEST_PUSH_WARNING = "Notification is a test push, not sending metrics"
    private val REQUEST_EXCEPTION_WARNING = "Error sending opened notification metric"
    private val INVALID_DEEP_LINK_URL = "Deep link url was null or empty"

    private val testActionName = "test.action.SOME_SHOUTING"
    private val testInstanceId = "some_instance_id_it_really_doesn't_matter_right_now"
    private val testPackageName = "com.testing.123"

    private lateinit var actionProcessor: ConnectActionProcessor

    @Mock
    private lateinit var mockMetricsProvider: MetricsProvider

    @Mock
    private lateinit var mockCall: Call<Void>

    @Mock
    private lateinit var mockIntent: Intent

    @Mock
    private lateinit var mockPayload: NotificationPayload

    @Mock
    private lateinit var mockPackageManager: PackageManager

    @Mock
    private lateinit var mockParcel: Parcel

    @Mock
    private lateinit var mockIntentBuilder: IntentBuilder

    @Mock
    private lateinit var mockUri: Uri

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    @Before
    fun setUp() {
        `when`(mockPayload.instanceId).thenReturn(testInstanceId)
        `when`(mockPackageManager.getLaunchIntentForPackage(anyString())).thenReturn(mockIntent)
        `when`(mockIntent.setData(any<Uri>())).then {  }

        `when`(mockParcel.readString()).thenReturn("some_string")
        `when`(mockParcel.readByte()).thenReturn(0)
        `when`(mockParcel.readInt()).thenReturn(0)

        `when`(mockIntentBuilder.withAction(anyString())).thenReturn(mockIntentBuilder)
        `when`(mockIntentBuilder.withData(anyString())).thenReturn(mockIntentBuilder)
        `when`(mockIntentBuilder.withData(any<Uri>())).thenReturn(mockIntentBuilder)
        `when`(mockIntentBuilder.withFlags(anyInt())).thenReturn(mockIntentBuilder)
        `when`(mockIntentBuilder.withPackageName(anyString())).thenReturn(mockIntentBuilder)
        `when`(mockIntentBuilder.parseUrl(anyString())).thenReturn(mockUri)
        `when`(mockIntentBuilder.build()).thenReturn(mockIntent)

        actionProcessor = ConnectActionProcessor(mockMetricsProvider)
    }

    @Test
    fun `extractPayload should return null for an invalid Intent`() {
        val extractedPayload = actionProcessor.extractPayload(null, testPackageName)

        assertThat(extractedPayload).isNull()
    }

    @Test
    fun `extractPayload should log a warning for an invalid Intent`() {
        actionProcessor.extractPayload(null, testPackageName)

        assertThat(logAppender.lastLog()).isEqualTo(INVALID_INTENT_WARNING)
    }

    @Test
    fun `extractPayload should return null for if the intent extra is invalid`() {
        val extractedPayload = actionProcessor.extractPayload(mockIntent, testPackageName)

        assertThat(extractedPayload).isNull()
    }

    @Test
    fun `extractPayload should return a NotificationPayload for a valid intent`() {
        `when`(mockIntent.getParcelableExtra<Parcelable>(anyString())).thenReturn(mockPayload)
        `when`(mockIntent.action).thenReturn(testPackageName + ConnectActionService.ACTION_OPEN_NOTIFICATION)

        val extractedPayload = actionProcessor.extractPayload(mockIntent, testPackageName)

        assertThat(extractedPayload).isNotNull()
    }

    @Test
    fun `extractPayloadFromIntent should return null for a null intent extra`() {
        `when`(mockIntent.getSerializableExtra(anyString())).thenReturn(null)

        val result = actionProcessor.extractPayloadFromIntent(mockIntent)

        assertThat(result).isNull()
    }

    @Test
    fun `extractPayloadFromIntent should return null for an invalid intent extra`() {
        val invalidExtra = object : Serializable {}
        `when`(mockIntent.getSerializableExtra(anyString())).thenReturn(invalidExtra)

        val result = actionProcessor.extractPayloadFromIntent(mockIntent)

        assertThat(result).isNull()
    }

    @Test
    fun `extractPayloadFromIntent should return an object for a valid intent extra`() {
        val validExtra = NotificationPayload(mockParcel)
        `when`(mockIntent.getParcelableExtra<NotificationPayload>(anyString())).thenReturn(validExtra)

        val result = actionProcessor.extractPayloadFromIntent(mockIntent)

        assertThat(result).isNotNull()
    }

    @Test
    fun `verifyIntent should return false for a null intent`() {
        val result = actionProcessor.verifyIntent(null, testActionName)

        assertThat(result).isFalse()
    }

    @Test
    fun `verifyIntent should return false for a null intent action`() {
        `when`(mockIntent.action).thenReturn(null)

        val result = actionProcessor.verifyIntent(mockIntent, testActionName)

        assertThat(result).isFalse()
    }

    @Test
    fun `verifyIntent should return false for an empty intent action`() {
        `when`(mockIntent.action).thenReturn("")

        val result = actionProcessor.verifyIntent(mockIntent, testActionName)

        assertThat(result).isFalse()
    }

    @Test
    fun `verifyIntent should return false for an invalid intent action`() {
        `when`(mockIntent.action).thenReturn("wildcard!")

        val result = actionProcessor.verifyIntent(mockIntent, testActionName)

        assertThat(result).isFalse()
    }

    @Test
    fun `verifyIntent should return true for a valid intent action`() {
        `when`(mockIntent.action).thenReturn(testActionName)

        val result = actionProcessor.verifyIntent(mockIntent, testActionName)

        assertThat(result).isTrue()
    }

    @Test
    fun `sendMetrics should log a warning for a null payload`() {
        actionProcessor.sendMetrics(null)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_PAYLOAD_WARNING)
    }

    @Test
    fun `sendMetrics should log a warning for an invalid payload`() {
        Mockito.reset(mockPayload)

        actionProcessor.sendMetrics(mockPayload)

        assertThat(logAppender.lastLog()).isEqualTo(INVALID_PAYLOAD_WARNING)
    }

    @Test
    fun `sendMetrics should log a warning for a test push`() {
        `when`(mockPayload.isTestPush).thenReturn(true)

        actionProcessor.sendMetrics(mockPayload)

        assertThat(logAppender.lastLog()).isEqualTo(TEST_PUSH_WARNING)
    }

    @Test
    fun `sendMetrics should log a warning if an IOException is encountered`() {
        `when`(mockMetricsProvider.opened(anyString(), any<PushBasicMetric>())).then {
            throw IOException()
        }

        actionProcessor.sendMetrics(mockPayload)

        assertThat(logAppender.lastLog()).contains(REQUEST_EXCEPTION_WARNING)
    }

    @Test
    fun `sendMetrics should send an opened notification metric requests`() {
        `when`(mockMetricsProvider.opened(anyString(), any<PushBasicMetric>())).thenReturn(mockCall)

        actionProcessor.sendMetrics(mockPayload)

        verify(mockMetricsProvider).opened(anyString(), any<PushBasicMetric>())
    }

    @Test
    fun `resolveDeepLinkIntent should return null for an empty deep link url`() {
        val resolvedIntent = actionProcessor.resolveDeepLinkIntent("", mockIntentBuilder)

        assertThat(resolvedIntent).isNull()
    }

    @Test
    fun `resolveDeepLinkIntent should log a warning for an empty deep link url`() {
        actionProcessor.resolveDeepLinkIntent("", mockIntentBuilder)

        assertThat(logAppender.lastLog()).isEqualTo(INVALID_DEEP_LINK_URL)
    }

    @Test
    fun `resolveDeepLinkIntent should return an intent for the deep link Uri`() {
        val deepLinkUrl = "https://www.google.com"
        val resolvedIntent = actionProcessor.resolveDeepLinkIntent(deepLinkUrl, mockIntentBuilder)

        assertThat(resolvedIntent).isNotNull()
    }

    @After
    fun tearDown() {

    }
}