package com.zendesk.connect.testapp.helpers

import com.zendesk.logger.Logger

class TestLogAppender: Logger.LogAppender {

    val logs = arrayListOf<String>()

    override fun log(priority: Logger.Priority?, tag: String?, message: String?, throwable: Throwable?) {
        logs.add(message.orEmpty())
    }

    fun lastLog(): String = logs.last()

    fun reset() = logs.clear()

}