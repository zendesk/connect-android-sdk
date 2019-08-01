package com.zendesk.connect

import android.content.Context
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class IpmPushStrategyFactoryTests {

    private val mockParser = mock<IpmPayloadParser>()
    private val mockContext = mock<Context>()
    private val mockIntentBuilder = mock<IntentBuilder>()

    private val factory = IpmPushStrategyFactory(mockParser, mockContext, mockIntentBuilder)

    @Test
    fun `create should return a nonnull instance of a push strategy`() {
        val strategy = factory.create()

        assertThat(strategy).isNotNull()
    }

    @Test
    fun `create should return an instance of IpmPushStrategy`() {
        val strategy = factory.create()

        assertThat(strategy).isInstanceOf(IpmPushStrategy::class.java)
    }

}
