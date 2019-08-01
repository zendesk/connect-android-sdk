package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import com.google.firebase.messaging.RemoteMessage
import com.zendesk.logger.Logger
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@RunWith(MockitoJUnitRunner.Silent::class)
class DefaultMessageReceiverTests {

    companion object {
        private const val LOG_RECEIVED_NOT_IMPLEMENTED = "onMessageReceived has not been implemented by the integrator"
        private const val LOG_DELETED_MESSAGES_NOT_IMPLEMENTED = "onDeletedMessages has not been implemented by the integrator"
        private const val LOG_MESSAGE_SENT_NOT_IMPLEMENTED = "onMessageSent has not been implemented by the integrator"
        private const val LOG_SEND_ERROR_NOT_IMPLEMENTED = "onSendError has not been implemented by the integrator"
        private const val LOG_NEW_TOKEN_NOT_IMPLEMENTED = "onNewToken has not been implemented by the integrator"
    }

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    private lateinit var messageReceiver: DefaultMessageReceiver
    private lateinit var messagingService: ConnectMessagingService

    @Mock private lateinit var mockRemoteMessage: RemoteMessage

    @Before
    fun setUp() {
        messagingService = ConnectMessagingService()
        messageReceiver = DefaultMessageReceiver()
    }

    @Test
    fun `onMessageReceived should log a warning for no provided implementation`() {
        messageReceiver.onMessageReceived(mockRemoteMessage, messagingService)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_RECEIVED_NOT_IMPLEMENTED)
    }

    @Test
    fun `onDeletedMessages should log a warning for no provided implementation`() {
        messageReceiver.onDeletedMessages(messagingService)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_DELETED_MESSAGES_NOT_IMPLEMENTED)
    }

    @Test
    fun `onMessageSent should log a warning for no provided implementation`() {
        messageReceiver.onMessageSent("some_message_id", messagingService)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_MESSAGE_SENT_NOT_IMPLEMENTED)
    }

    @Test
    fun `onSendError should log a warning for no provided implementation`() {
        messageReceiver.onSendError("some_message_id", IOException(), messagingService)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_SEND_ERROR_NOT_IMPLEMENTED)
    }

    @Test
    fun `onNewToken should log a warning for no provided implementation`() {
        messageReceiver.onNewToken("some_new_token", messagingService)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_NEW_TOKEN_NOT_IMPLEMENTED)
    }
}