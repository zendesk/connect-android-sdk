package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.BDDMockito.willReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

class IpmMetricProcessorTests {

    private val mockMetricRequestsProcessor = mock<MetricRequestsProcessor>()
    private val mockConnectClient = mock<ConnectClient>()

    private val ipmEventManager = spy(IpmMetricProcessor(
        mockMetricRequestsProcessor,
        mockConnectClient
    ))

    private val mockInstanceId = "some-id"

    @Test
    fun `EVENT_ACTION value should be correct `() {
        assertThat(IpmMetricProcessor.EVENT_ACTION).isEqualTo("ipm_metric_action_tapped")
    }

    @Test
    fun `EVENT_DISMISS value should be correct `() {
        assertThat(IpmMetricProcessor.EVENT_DISMISS).isEqualTo("ipm_metric_dismissed")
    }

    @Test
    fun `trackDisplayed should send a received request`() {
        ipmEventManager.trackDisplayed(mockInstanceId)

        verify(mockMetricRequestsProcessor).sendReceivedRequest(mockInstanceId)
    }

    @Test
    fun `trackDisplayed should send an opened request`() {
        ipmEventManager.trackDisplayed(mockInstanceId)

        verify(mockMetricRequestsProcessor).sendOpenedRequest(mockInstanceId, false)
    }

    @Test
    fun `trackAction should track an action event`() {
        val expectedEvent = Event(null, IpmMetricProcessor.EVENT_ACTION, null, 0)
        willReturn(expectedEvent)
            .given(ipmEventManager).createEvent(IpmMetricProcessor.EVENT_ACTION)

        ipmEventManager.trackAction()

        verify(mockConnectClient).trackEvent(expectedEvent)
    }

    @Test
    fun `trackDismiss should track a dismiss event`() {
        val expectedEvent = Event(null, IpmMetricProcessor.EVENT_DISMISS, null, 0)
        willReturn(expectedEvent)
            .given(ipmEventManager).createEvent(IpmMetricProcessor.EVENT_DISMISS)

        ipmEventManager.trackDismiss()

        verify(mockConnectClient).trackEvent(expectedEvent)
    }
}
