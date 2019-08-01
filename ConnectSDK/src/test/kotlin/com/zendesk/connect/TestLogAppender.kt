package com.zendesk.connect

import com.zendesk.logger.Logger

class TestLogAppender: Logger.LogAppender {

    val logs = arrayListOf<String>()

    override fun log(priority: Logger.Priority?, tag: String?, message: String?, throwable: Throwable?) {
        logs.add(message.orEmpty())
    }

    fun contains(message: String): Boolean = logs.contains(message)

    fun lastLog(): String = logs.last()

    fun reset() = logs.clear()

}
