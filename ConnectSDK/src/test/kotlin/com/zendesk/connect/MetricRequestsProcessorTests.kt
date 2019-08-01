package com.zendesk.connect

import androidx.core.app.NotificationManagerCompat
import com.google.common.truth.Truth
import com.zendesk.logger.Logger
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

class MetricRequestsProcessorTests {

    companion object {
        private const val TEST_PUSH_LOG = "Notification is a test push, not sending metrics"
        private const val NOT_CONNECT_PUSH_LOG = "Payload is not a valid Connect notification"
        private const val ERROR_REQUEST_LOG = "There was a problem sending the metric request:"
        private const val UNSUCCESSFUL_REQUEST_LOG = "Metric request unsuccessful. Response code:"
        private const val NULL_CALL_LOG = "Call was null, couldn't send request"
    }

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    private val mockInstanceId = "some-instance-id"

    private val mockMetricsProvider = mock<MetricsProvider>()
    private val mockCall = mock<Call<Void>>()
    private val mockResponse = mock<Response<Void>>()
    private val mockManagerCompat = mock<NotificationManagerCompat>()

    private val metricsProcessor = MetricRequestsProcessor(mockMetricsProvider)

    @Before
    fun setUp() {
        given(mockMetricsProvider.received(Mockito.anyString(), ArgumentMatchers.any<PushBasicMetric>())).willReturn(mockCall)
        given(mockCall.execute()).willReturn(mockResponse)
        given(mockResponse.isSuccessful).willReturn(true)
        given(mockManagerCompat.areNotificationsEnabled()).willReturn(true)
    }

    @Test
    fun `sendOpenedRequest should log a message if isTestPush is true`() {
        metricsProcessor.sendOpenedRequest(null, true)

        Truth.assertThat(logAppender.lastLog()).isEqualTo(TEST_PUSH_LOG)
    }

    @Test
    fun `sendOpenedRequest should log a message if instance id is null`() {
        metricsProcessor.sendOpenedRequest(null, false)

        Truth.assertThat(logAppender.lastLog()).isEqualTo(NOT_CONNECT_PUSH_LOG)
    }

    @Test
    fun `sendOpenedRequest should create a call object for a received request`() {
        metricsProcessor.sendOpenedRequest(mockInstanceId, false)

        Mockito.verify(mockMetricsProvider).opened(Mockito.anyString(), ArgumentMatchers.any<PushBasicMetric>())
    }

    @Test
    fun `sendOpenedRequest should add the notification instance id to the request body`() {
        val captor = ArgumentCaptor.forClass(PushBasicMetric::class.java)

        metricsProcessor.sendOpenedRequest(mockInstanceId, false)

        Mockito.verify(mockMetricsProvider).opened(Mockito.anyString(), captor.capture())
        Truth.assertThat(captor.value.oid).isEqualTo(mockInstanceId)
    }

    @Test
    fun `sendReceivedRequest should create a call object for a received request`() {
        metricsProcessor.sendReceivedRequest(mockInstanceId)

        Mockito.verify(mockMetricsProvider).received(Mockito.anyString(), ArgumentMatchers.any<PushBasicMetric>())
    }

    @Test
    fun `sendReceivedRequest should add the notification instance id to the request body`() {
        val captor = ArgumentCaptor.forClass(PushBasicMetric::class.java)

        metricsProcessor.sendReceivedRequest(mockInstanceId)

        Mockito.verify(mockMetricsProvider).received(Mockito.anyString(), captor.capture())
        Truth.assertThat(captor.value.oid).isEqualTo(mockInstanceId)
    }

    @Test
    fun `sendUninstallTracker should create a call object for an uninstall tracker request`() {
        metricsProcessor.sendUninstallTrackerRequest(mockInstanceId, false)

        Mockito.verify(mockMetricsProvider).uninstallTracker(Mockito.anyString(), ArgumentMatchers.any<UninstallTracker>())
    }

    @Test
    fun `sendUninstallTracker should add the notifications revoked flag to the request body`() {
        val revoked = true
        val captor = ArgumentCaptor.forClass(UninstallTracker::class.java)

        metricsProcessor.sendUninstallTrackerRequest(mockInstanceId, revoked)

        Mockito.verify(mockMetricsProvider).uninstallTracker(Mockito.anyString(), captor.capture())
        Truth.assertThat(captor.value.isRevoked).isEqualTo(revoked)
    }

    @Test
    fun `sendRequest should log a warning if the given call is null`() {
        metricsProcessor.sendRequest(null)

        Truth.assertThat(logAppender.lastLog()).isEqualTo(NULL_CALL_LOG)
    }

    @Test
    fun `sendRequest should log a warning if the call given throws an IOException`() {
        Mockito.`when`(mockCall.execute()).then { throw IOException() }

        metricsProcessor.sendRequest(mockCall)

        Truth.assertThat(logAppender.lastLog()).contains(ERROR_REQUEST_LOG)
    }

    @Test
    fun `sendRequest should log a warning if the request is unsuccessful`() {
        Mockito.`when`(mockResponse.isSuccessful).thenReturn(false)

        metricsProcessor.sendRequest(mockCall)

        Truth.assertThat(logAppender.lastLog()).contains(UNSUCCESSFUL_REQUEST_LOG)
    }

    @Test
    fun `sendRequest should execute the call given to it`() {
        metricsProcessor.sendRequest(mockCall)

        Mockito.verify(mockCall).execute()
    }

}
