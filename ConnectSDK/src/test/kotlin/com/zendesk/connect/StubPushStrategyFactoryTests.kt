package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class StubPushStrategyFactoryTests {

    private lateinit var factory: StubPushStrategyFactory

    @Before
    fun setUp() {
        factory = StubPushStrategyFactory()
    }

    @Test
    fun `create should return an instance of stub push strategy`() {
        val strategy = factory.create()

        assertThat(strategy).isInstanceOf(StubPushStrategy::class.java)
    }

    @Test
    fun `create should return a cached strategy on subsequent calls`() {
        val strategy1 = factory.create()
        val strategy2 = factory.create()

        assertThat(strategy1).isEqualTo(strategy2)
    }

}