package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class SystemPushStrategyFactoryTests {

    private val mockManager = mock<NotificationManager>()
    private val mockBuilder = mock<NotificationBuilder>()
    private val mockMetricsProcessor = mock<MetricRequestsProcessor>()
    private val mockEventListener = mock<NotificationEventListener>()
    private val mockFactory = mock<NotificationFactory>()
    private val mockParser = mock<SystemPushPayloadParser>()

    private lateinit var factory: SystemPushStrategyFactory

    @Before
    fun setUp() {
        factory = SystemPushStrategyFactory(mockManager, mockBuilder, mockMetricsProcessor, mockParser)
    }

    @Test
    fun `create should return an instance of system push strategy`() {
        val strategy = factory.create(mockEventListener, mockFactory)

        assertThat(strategy).isInstanceOf(SystemPushStrategy::class.java)
    }

    @Test
    fun `create should return a cached strategy on subsequent calls`() {
        val strategy1 = factory.create(mockEventListener, mockFactory)
        val strategy2 = factory.create(mockEventListener, mockFactory)

        assertThat(strategy1).isEqualTo(strategy2)
    }

}