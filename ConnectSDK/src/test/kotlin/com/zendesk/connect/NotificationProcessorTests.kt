package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import com.zendesk.logger.Logger
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class NotificationProcessorTests {

    companion object {
        private const val LOG_FORMAT_UNABLE_TO_HANDLE = "Couldn't create push strategy for %s payload"
    }

    private val systemPushData = mapOf(Pair(ConnectNotification.Keys.INSTANCE_ID.key, "1234"))
    private val ipmData = systemPushData.plus(Pair(ConnectNotification.Keys.TYPE.key, "ipm"))
    private val unknownPushData = mapOf(Pair("bonus points", "1,000,000"))

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    private val mockStubPushStrategyFactory = mock<StubPushStrategyFactory>()
    private val mockStubPushStrategy = mock<StubPushStrategy>()
    private val mockSystemPushStrategyFactory = mock<SystemPushStrategyFactory>()
    private val mockSystemPushStrategy = mock<SystemPushStrategy>()
    private val mockIpmPushStrategyFactory = mock<IpmPushStrategyFactory>()
    private val mockIpmPushStrategy = mock<IpmPushStrategy>()
    private val mockNotificationEventListener = mock<NotificationEventListener>()
    private val mockNotificationFactory = mock<NotificationFactory>()

    private lateinit var notificationProcessor: NotificationProcessor

    @Before
    fun setUp() {
        NotificationProcessor.setNotificationEventListener(mockNotificationEventListener)
        NotificationProcessor.setNotificationFactory(mockNotificationFactory)

        given(mockStubPushStrategyFactory.create())
                .willReturn(mockStubPushStrategy)
        given(mockSystemPushStrategyFactory.create(mockNotificationEventListener, mockNotificationFactory))
                .willReturn(mockSystemPushStrategy)
        given(mockIpmPushStrategyFactory.create())
                .willReturn(mockIpmPushStrategy)

        notificationProcessor = NotificationProcessor(
                mockStubPushStrategyFactory,
                mockSystemPushStrategyFactory,
                mockIpmPushStrategyFactory,
                mockNotificationEventListener,
                mockNotificationFactory
        )
    }

    @Test
    fun `process should query the system push strategy factory for a SYSTEM_PUSH type payload`() {
        notificationProcessor.process(systemPushData)

        verify(mockSystemPushStrategyFactory).create(mockNotificationEventListener, mockNotificationFactory)
    }

    @Test
    fun `process should invoke a system push strategy for a SYSTEM_PUSH type payload`() {
        notificationProcessor.process(systemPushData)

        verify(mockSystemPushStrategy).process(systemPushData)
    }

    @Test
    fun `process should query the stub push strategy factory for an IPM type payload`() {
        notificationProcessor.process(ipmData)

        verify(mockIpmPushStrategyFactory).create()
    }

    @Test
    fun `process should invoke an ipm basic push strategy for an IPM type payload`() {
        notificationProcessor.process(ipmData)

        verify(mockIpmPushStrategy).process(ipmData)
    }

    @Test
    fun `process should query the stub push strategy factory for an UNKNOWN type payload`() {
        notificationProcessor.process(unknownPushData)

        verify(mockStubPushStrategyFactory).create()
    }

    @Test
    fun `process should log a warning that a strategy was not available for UNKNOWN payload`() {
        notificationProcessor.process(unknownPushData)

        assertThat(logAppender.lastLog()).isEqualTo(
                String.format(LOG_FORMAT_UNABLE_TO_HANDLE, ConnectNotification.Types.UNKNOWN.name)
        )
    }

}
