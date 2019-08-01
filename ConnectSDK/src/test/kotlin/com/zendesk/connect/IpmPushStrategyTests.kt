package com.zendesk.connect

import android.content.Context
import android.content.Intent
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

class IpmPushStrategyTests {

    private val data = mapOf(
        "header" to "Connect: In-app Messages",
        "body" to "In-App Messages let you display a message to your customers",
        "_oid" to "1",
        "ttl" to "999",
        "pushFallback" to "true",
        "logo" to "https://dreager1.files.wordpress.com/2015/11/121106.jpg",
        "buttonText" to "Ok",
        "titleFontColor" to "#000000",
        "bodyFontColor" to "#737373",
        "backgroundColor" to "#ffffff",
        "buttonBackgroundColor" to "#1f73b7",
        "buttonTextColor" to "#ffffff",
        "type" to "ipm",
        "action" to "https://www.google.com"
    )

    private val mockParser = mock<IpmPayloadParser>()
    private val mockPayload = mock<IpmPayload>()
    private val mockIntentBuilder = mock<IntentBuilder>()
    private val mockContext = mock<Context>()
    private val mockIpm = mock<IpmPayload>()
    private val mockIntent = mock<Intent>()

    private val strategy = spy(IpmPushStrategy(mockParser, mockContext, mockIntentBuilder))

    private lateinit var testData: MutableMap<String, String>

    @Before
    fun setUp() {
        testData = HashMap(data)

        given(mockParser.parse(testData)).willReturn(mockPayload)
        given(mockIntentBuilder.build()).willReturn(mockIntent)
        willDoNothing().given(strategy).enqueueWork(mockIntent)
    }

    @Test
    fun `process should parse the received payload`() {
        strategy.process(testData)

        verify(mockParser).parse(testData)
    }

    @Test
    fun `process should put the parsed payload into the service intent`() {
        given(mockParser.parse(testData)).willReturn(mockIpm)

        strategy.process(testData)

        verify(mockIntentBuilder).withExtra(ConnectIpmService.IPM_PAYLOAD_PARCELABLE_KEY, mockIpm)
    }

    @Test
    fun `process should enqueue work`() {
        strategy.process(testData)

        verify(strategy).enqueueWork(mockIntent)
    }
}
