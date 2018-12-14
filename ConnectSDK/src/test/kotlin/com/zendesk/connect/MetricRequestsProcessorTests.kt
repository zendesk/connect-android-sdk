package com.zendesk.connect

import android.support.v4.app.NotificationManagerCompat
import com.google.common.truth.Truth
import com.zendesk.logger.Logger
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

@RunWith(MockitoJUnitRunner.Silent::class)
class MetricRequestsProcessorTests {

    private val NULL_METRICS_PROVIDER = "Metrics provider was null, unable to send metrics requests"
    private val NULL_NOTIFICATION_MANAGER_COMPAT = "Notification manager compat was null, unable to send uninstall tracker"
    private val ERROR_REQUEST_LOG = "There was a problem sending the metric request:"
    private val UNSUCCESSFUL_REQUEST_LOG = "Metric request unsuccessful. Response code:"
    private val NULL_CALL_LOG = "Call was null, couldn't send request"

    private lateinit var metricsProcessor: MetricRequestsProcessor

    @Mock
    private lateinit var mockPayload: NotificationPayload

    @Mock
    private lateinit var mockMetricsProvider: MetricsProvider

    @Mock
    private lateinit var mockCall: Call<Void>

    @Mock
    private lateinit var mockResponse: Response<Void>

    @Mock
    private lateinit var mockManagerCompat: NotificationManagerCompat

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(MetricRequestsProcessorTests::class)

        `when`(mockMetricsProvider.received(Mockito.anyString(), ArgumentMatchers.any<PushBasicMetric>())).thenReturn(mockCall)
        `when`(mockCall.execute()).thenReturn(mockResponse)
        `when`(mockResponse.isSuccessful).thenReturn(true)
        `when`(mockManagerCompat.areNotificationsEnabled()).thenReturn(true)

        metricsProcessor = MetricRequestsProcessor(mockMetricsProvider)
    }

    @Test
    fun `sendReceivedRequest should create a call object for a received request`() {
        metricsProcessor.sendReceivedRequest(mockPayload)

        Mockito.verify(mockMetricsProvider).received(Mockito.anyString(), ArgumentMatchers.any<PushBasicMetric>())
    }

    @Test
    fun `sendReceivedRequest should add the notification instance id to the request body`() {
        Mockito.`when`(mockPayload.instanceId).thenReturn("c_137")
        val captor = ArgumentCaptor.forClass(PushBasicMetric::class.java)

        metricsProcessor.sendReceivedRequest(mockPayload)

        Mockito.verify(mockMetricsProvider).received(Mockito.anyString(), captor.capture())
        Truth.assertThat(captor.value.oid).isEqualTo(mockPayload.instanceId)
    }

    @Test
    fun `sendUninstallTracker should create a call object for an uninstall tracker request`() {
        metricsProcessor.sendUninstallTrackerRequest(mockPayload, false)

        Mockito.verify(mockMetricsProvider).uninstallTracker(Mockito.anyString(), ArgumentMatchers.any<UninstallTracker>())
    }

    @Test
    fun `sendUninstallTracker should add the notifications revoked flag to the request body`() {
        val revoked = true
        val captor = ArgumentCaptor.forClass(UninstallTracker::class.java)

        metricsProcessor.sendUninstallTrackerRequest(mockPayload, revoked)

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