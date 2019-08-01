package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import com.zendesk.logger.Logger
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class DefaultNotificationFactoryTests {

    companion object {
        private const val LOG_NO_IMPLEMENTATION = "Custom notification has not been provided"
    }

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    private lateinit var notificationFactory: DefaultNotificationFactory
    private lateinit var messagingService: ConnectMessagingService

    @Mock private lateinit var mockPayload: SystemPushPayload

    @Before
    fun setUp() {
        messagingService = ConnectMessagingService()
        notificationFactory = DefaultNotificationFactory()
    }

    @Test
    fun `provide should return null`() {
        val notification = notificationFactory.create(mockPayload)

        assertThat(notification).isNull()
    }

    @Test
    fun `provide should log a message for no implementation`() {
        notificationFactory.create(mockPayload)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_NO_IMPLEMENTATION)
    }

}