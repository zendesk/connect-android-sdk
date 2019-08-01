package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import com.zendesk.logger.Logger
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class DefaultNotificationEventListenerTests {

    companion object {
        private const val LOG_RECEIVE_NOT_IMPLEMENTED = "onNotificationReceived has not been implemented by the integrator"
        private const val LOG_DISPLAY_NOT_IMPLEMENTED = "onNotificationDisplayed has not been implemented by the integrator"
    }

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    private lateinit var notificationEventListener: DefaultNotificationEventListener
    private lateinit var messagingService: ConnectMessagingService

    @Mock private lateinit var mockPayload: SystemPushPayload

    @Before
    fun setUp() {
        messagingService = ConnectMessagingService()
        notificationEventListener = DefaultNotificationEventListener()
    }

    @Test
    fun `onNotificationReceived should log a message for no provided implementation`() {
        notificationEventListener.onNotificationReceived(mockPayload)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_RECEIVE_NOT_IMPLEMENTED)
    }

    @Test
    fun `onNotificationDisplayed should log a message for no provided implementation`() {
        notificationEventListener.onNotificationDisplayed(mockPayload)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_DISPLAY_NOT_IMPLEMENTED)
    }

}