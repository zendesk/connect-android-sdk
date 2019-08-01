package com.zendesk.connect

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Parcelable
import com.google.common.truth.Truth.assertThat
import com.zendesk.logger.Logger
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willReturn
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import java.io.Serializable

class ConnectActionProcessorTests {

    companion object {
        private const val NULL_PACKAGE_MANAGER_WARNING = "Package manager was null, unable to resolve intent"
        private const val NULL_PAYLOAD_WARNING = "Payload was null, unable to resolve intent"
        private const val INVALID_INTENT_WARNING = "Intent was null or contained invalid action name"
        private const val INVALID_DEEP_LINK_URL = "Deep link url was null or empty"
        private const val FAILED_TO_RESOLVE_ACTIVITY = "Intent for url some://deep/link couldn't be resolved to any Activity"
    }

    private val testActionName = "test.action.SOME_SHOUTING"
    private val testPackageName = "com.testing.123"
    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    private val mockMetricRequestsProcessor = mock<MetricRequestsProcessor>()
    private val mockPackageManager = mock<PackageManager>()
    private val mockIntentBuilder = mock<IntentBuilder>()

    private val actionProcessor = spy(ConnectActionProcessor(
        mockMetricRequestsProcessor,
        mockPackageManager,
        mockIntentBuilder
    ))

    private val mockIntent = mock<Intent>()
    private val mockPayload = mock<SystemPushPayload>()
    private val mockDeepLinkUrl = "some://deep/link"

    @Before
    fun setUp() {
        given(mockPackageManager.getLaunchIntentForPackage(anyString())).willReturn(mockIntent)
        given(mockIntentBuilder.withAction(anyString())).willReturn(mockIntentBuilder)
        given(mockIntentBuilder.withData(anyString())).willReturn(mockIntentBuilder)
        given(mockIntentBuilder.withFlags(anyInt())).willReturn(mockIntentBuilder)
        given(mockIntentBuilder.from(any())).willReturn(mockIntentBuilder)
        given(mockIntentBuilder.build()).willReturn(mockIntent)
    }

    @Test
    fun `resolveIntent should return null if the package manager is null`() {
        val altActionProcessor = ConnectActionProcessor(mockMetricRequestsProcessor, null, mockIntentBuilder)

        val result = altActionProcessor.resolveIntent(mockPayload, testPackageName, true, true)

        assertThat(result).isNull()
    }

    @Test
    fun `resolveIntent should log a warning if the package manager is null`() {
        val altActionProcessor = ConnectActionProcessor(mockMetricRequestsProcessor, null, mockIntentBuilder)

        altActionProcessor.resolveIntent(mockPayload, testPackageName, true, true)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_PACKAGE_MANAGER_WARNING)
    }

    @Test
    fun `resolveIntent should return null if the payload is null`() {
        val result = actionProcessor.resolveIntent(null, testPackageName, true, true)

        assertThat(result).isNull()
    }

    @Test
    fun `resolveIntent should log a warning if the payload is null`() {
        actionProcessor.resolveIntent(null, testPackageName, true, true)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_PAYLOAD_WARNING)
    }

    @Test
    fun `resolveIntent should return null if open launch activity is true but the launch intent for the package is null`() {
        given(mockPackageManager.getLaunchIntentForPackage(testPackageName)).willReturn(null)

        val result = actionProcessor.resolveIntent(mockPayload, testPackageName, true, false)

        assertThat(result).isNull()
    }

    @Test
    fun `resolveIntent should return an intent if open launch activity is true`() {
        val result = actionProcessor.resolveIntent(mockPayload, testPackageName, true, false)

        assertThat(result).isEqualTo(mockIntent)
    }

    @Test
    fun `resolveIntent should return an intent created from the launch intent for the package if open launch activity is true`() {
        actionProcessor.resolveIntent(mockPayload, testPackageName, true, false)

        verify(mockIntentBuilder).from(mockIntent)
    }

    @Test
    fun `resolveIntent should return an intent that has flags set if open launch activity is true`() {
        actionProcessor.resolveIntent(mockPayload, testPackageName, true, false)

        verify(mockIntentBuilder).withFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    @Test
    fun `resolveIntent should return null if the payload deeplink url was null`() {
        given(mockPayload.deeplinkUrl).willReturn(null)

        val result = actionProcessor.resolveIntent(mockPayload, testPackageName, false, true)

        assertThat(result).isNull()
    }

    @Test
    fun `resolveIntent should return null if the deep link url intent cannot be resolved`() {
        given(mockPayload.deeplinkUrl).willReturn(mockDeepLinkUrl)
        willReturn(null).given(actionProcessor).resolveDeepLinkIntent(mockDeepLinkUrl)

        val result = actionProcessor.resolveIntent(mockPayload, testPackageName, false, true)

        assertThat(result).isNull()
    }

    @Test
    fun `resolveIntent should return the intent from the deeplink url if both openLaunchActivity and handleDeepLinks are true`() {
        val deepLinkIntent: Intent = mock()

        given(mockPayload.deeplinkUrl).willReturn(mockDeepLinkUrl)
        willReturn(deepLinkIntent).given(actionProcessor).resolveDeepLinkIntent(mockDeepLinkUrl)

        val result = actionProcessor.resolveIntent(mockPayload, testPackageName, true, true)

        assertThat(result).isEqualTo(deepLinkIntent)
    }

    @Test
    fun `resolveIntent should return null if openLaunchActivity is false and handleDeepLinks is false`() {
        val result = actionProcessor.resolveIntent(mockPayload, testPackageName, false, false)

        assertThat(result).isNull()
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
        given(mockIntent.getParcelableExtra<Parcelable>(anyString())).willReturn(mockPayload)
        given(mockIntent.action).willReturn(testPackageName + ConnectActionService.ACTION_OPEN_NOTIFICATION)

        val extractedPayload = actionProcessor.extractPayload(mockIntent, testPackageName)

        assertThat(extractedPayload).isNotNull()
    }

    @Test
    fun `extractPayload should not send metrics for an invalid payload`() {
        actionProcessor.extractPayload(null, testPackageName)

        verify(mockMetricRequestsProcessor, never()).sendOpenedRequest(anyString(), anyBoolean())
    }

    @Test
    fun `extractPayload should send metrics for a valid payload`() {
        val mockInstanceId = "some-instance-id"
        val isTestPush = true

        given(mockIntent.getParcelableExtra<Parcelable>(anyString())).willReturn(mockPayload)
        given(mockIntent.action).willReturn(testPackageName + ConnectActionService.ACTION_OPEN_NOTIFICATION)
        given(mockPayload.instanceId).willReturn(mockInstanceId)
        given(mockPayload.isTestPush).willReturn(isTestPush)

        actionProcessor.extractPayload(mockIntent, testPackageName)

        verify(mockMetricRequestsProcessor).sendOpenedRequest(mockInstanceId, isTestPush)
    }

    @Test
    fun `extractPayloadFromIntent should return null for a null intent extra`() {
        given(mockIntent.getSerializableExtra(anyString())).willReturn(null)

        val result = actionProcessor.extractPayloadFromIntent(mockIntent)

        assertThat(result).isNull()
    }

    @Test
    fun `extractPayloadFromIntent should return null for an invalid intent extra`() {
        val invalidExtra = object : Serializable {}
        given(mockIntent.getSerializableExtra(anyString())).willReturn(invalidExtra)

        val result = actionProcessor.extractPayloadFromIntent(mockIntent)

        assertThat(result).isNull()
    }

    @Test
    fun `extractPayloadFromIntent should return an object for a valid intent extra`() {
        val validExtra = mock<SystemPushPayload>()
        given(mockIntent.getParcelableExtra<SystemPushPayload>(anyString())).willReturn(validExtra)

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
        given(mockIntent.action).willReturn(null)

        val result = actionProcessor.verifyIntent(mockIntent, testActionName)

        assertThat(result).isFalse()
    }

    @Test
    fun `verifyIntent should return false for an empty intent action`() {
        given(mockIntent.action).willReturn("")

        val result = actionProcessor.verifyIntent(mockIntent, testActionName)

        assertThat(result).isFalse()
    }

    @Test
    fun `verifyIntent should return false for an invalid intent action`() {
        given(mockIntent.action).willReturn("wildcard!")

        val result = actionProcessor.verifyIntent(mockIntent, testActionName)

        assertThat(result).isFalse()
    }

    @Test
    fun `verifyIntent should return true for a valid intent action`() {
        given(mockIntent.action).willReturn(testActionName)

        val result = actionProcessor.verifyIntent(mockIntent, testActionName)

        assertThat(result).isTrue()
    }

    @Test
    fun `resolveDeepLinkIntent should return null for an empty deep link url`() {
        val resolvedIntent = actionProcessor.resolveDeepLinkIntent("")

        assertThat(resolvedIntent).isNull()
    }

    @Test
    fun `resolveDeepLinkIntent should log a warning for an empty deep link url`() {
        actionProcessor.resolveDeepLinkIntent("")

        assertThat(logAppender.lastLog()).isEqualTo(INVALID_DEEP_LINK_URL)
    }

    @Test
    fun `resolveDeepLinkIntent should call withAction in the IntentBuilder`() {
        actionProcessor.resolveDeepLinkIntent(mockDeepLinkUrl)

        verify(mockIntentBuilder).withAction(Intent.ACTION_VIEW)
    }

    @Test
    fun `resolveDeepLinkIntent should call withData in the IntentBuilder`() {
        actionProcessor.resolveDeepLinkIntent(mockDeepLinkUrl)

        verify(mockIntentBuilder).withData(mockDeepLinkUrl)
    }

    @Test
    fun `resolveDeepLinkIntent should call withFlags in the IntentBuilder`() {
        actionProcessor.resolveDeepLinkIntent(mockDeepLinkUrl)

        verify(mockIntentBuilder).withFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    @Test
    fun `resolveDeepLinkIntent should log a message if the activity for the deep link intent cannot be resolved`() {
        given(mockIntent.resolveActivity(mockPackageManager)).willReturn(null)

        actionProcessor.resolveDeepLinkIntent(mockDeepLinkUrl)

        assertThat(logAppender.lastLog()).isEqualTo(FAILED_TO_RESOLVE_ACTIVITY)
    }

    @Test
    fun `resolveDeepLinkIntent should return null if the activity for the deep link intent cannot be resolved`() {
        given(mockIntent.resolveActivity(mockPackageManager)).willReturn(null)

        val result = actionProcessor.resolveDeepLinkIntent(mockDeepLinkUrl)

        assertThat(result).isNull()
    }

    @Test
    fun `resolveDeepLinkIntent should return an intent if the activity for the deep link intent can be resolved`() {
        given(mockIntent.resolveActivity(mockPackageManager)).willReturn(mock())

        val result = actionProcessor.resolveDeepLinkIntent(mockDeepLinkUrl)

        assertThat(result).isEqualTo(mockIntent)
    }
}
