package com.zendesk.connect

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import com.zendesk.logger.Logger
import org.junit.Test

class ConnectIpmServiceTests {

    companion object {
        private const val LOG_NOT_INITIALISED = "Connect was not initialised, ending IPM job service"
    }

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    private val mockIntent = mock<Intent>()

    private val service = ConnectIpmService()

    @Test
    fun `onHandleIntent should log an error if Connect is not initialised`() {
        service.onHandleWork(mockIntent)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_NOT_INITIALISED)
    }
}
