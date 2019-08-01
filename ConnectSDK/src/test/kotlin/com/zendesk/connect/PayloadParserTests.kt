package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class PayloadParserTests {

    private val gson = Gson()
    private val payloadFields = SystemPushPayload::class.java.declaredFields
            .filter { it.getAnnotation(SerializedName::class.java) != null }
            .map { it.getAnnotation(SerializedName::class.java).value }

    private lateinit var payloadParser: StubPayloadParser<SystemPushPayload>

    @Before
    fun setUp() {
        payloadParser = spy(StubPayloadParser(SystemPushPayload::class.java, gson))
    }

    @Test
    fun `extract field names should return a set of all the annotated fields on SystemPushPayload`() {
        val fields = payloadParser.extractFieldNames()

        assertThat(fields.containsAll(payloadFields)).isTrue()
    }

    @Test
    fun `extract field names should only be called on the first call to getPayloadFields`() {
        payloadParser.payloadFields
        payloadParser.payloadFields
        payloadParser.payloadFields

        verify(payloadParser, times(1)).extractFieldNames()
    }

    /**
     * Stub implementation of [PayloadParser] for testing
     */
    private class StubPayloadParser<T>(clazz: Class<T>, gson: Gson) : PayloadParser<T>(clazz, gson)

}