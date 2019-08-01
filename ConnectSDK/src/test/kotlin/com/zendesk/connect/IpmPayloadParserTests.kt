package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class IpmPayloadParserTests {

    private val gson = Gson()
    private val data = mapOf(
            "heading" to "Connect: In-app Messages",
            "message" to "In-App Messages let you display a message to your customers",
            "_oid" to "1",
            "ttl" to "999",
            "pushFallback" to "true",
            "logo" to "https://dreager1.files.wordpress.com/2015/11/121106.jpg",
            "buttonText" to "Ok",
            "headingFontColor" to "#000000",
            "messageFontColor" to "#737373",
            "backgroundColor" to "#ffffff",
            "buttonBackgroundColor" to "#1f73b7",
            "buttonTextColor" to "#ffffff",
            "type" to "ipm",
            "action" to "https://www.google.com"
    )

    private val parser = IpmPayloadParser(gson)

    private lateinit var testData: MutableMap<String, String>

    @Before
    fun setUp() {
        testData = HashMap(data)
    }

    @Test
    fun `parse should return a non null instance of IpmPayload`() {
        val payload = parser.parse(testData)

        assertThat(payload).isNotNull()
    }

    @Test
    fun `parse should populate the model with the received header`() {
        val payload = parser.parse(testData)

        assertThat(payload?.heading).isEqualTo(data.getValue("heading").toString())
    }

    @Test
    fun `parse should populate the model with the received heading`() {
        val payload = parser.parse(testData)

        assertThat(payload?.message).isEqualTo(data.getValue("message").toString())
    }

    @Test
    fun `parse should populate the model with the received instance id`() {
        val payload = parser.parse(testData)

        assertThat(payload?.instanceId).isEqualTo(data.getValue("_oid").toString())
    }

    @Test
    fun `parse should populate the model with the received time to live`() {
        val payload = parser.parse(testData)

        assertThat(payload?.timeToLive).isEqualTo(data.getValue("ttl").toLong())
    }

    @Test
    fun `parse should populate the model with the received logo url`() {
        val payload = parser.parse(testData)

        assertThat(payload?.logo).isEqualTo(data.getValue("logo").toString())
    }

    @Test
    fun `parse should populate the model with the received button text`() {
        val payload = parser.parse(testData)

        assertThat(payload?.buttonText).isEqualTo(data.getValue("buttonText").toString())
    }

    @Test
    fun `parse should populate the model with the received heading font color`() {
        val payload = parser.parse(testData)

        assertThat(payload?.headingFontColor).isEqualTo(data.getValue("headingFontColor").toString())
    }

    @Test
    fun `parse should populate the model with the received message font color`() {
        val payload = parser.parse(testData)

        assertThat(payload?.messageFontColor).isEqualTo(data.getValue("messageFontColor").toString())
    }

    @Test
    fun `parse should populate the model with the received background color`() {
        val payload = parser.parse(testData)

        assertThat(payload?.backgroundColor).isEqualTo(data.getValue("backgroundColor").toString())
    }

    @Test
    fun `parse should populate the model with the received button background color`() {
        val payload = parser.parse(testData)

        assertThat(payload?.buttonBackgroundColor).isEqualTo(data.getValue("buttonBackgroundColor").toString())
    }

    @Test
    fun `parse should populate the model with the received button text color`() {
        val payload = parser.parse(testData)

        assertThat(payload?.buttonTextColor).isEqualTo(data.getValue("buttonTextColor").toString())
    }

    @Test
    fun `parse should populate the model with the received action url`() {
        val payload = parser.parse(testData)

        assertThat(payload?.action).isEqualTo(data.getValue("action").toString())
    }

    @Test
    fun `parse should return null for an invalid ttl value`() {
        testData["ttl"] = "hello"
        val payload = parser.parse(testData)

        assertThat(payload).isNull()
    }

    @Test
    fun `parse should return null for a missing ttl value`() {
        testData.remove("ttl")
        val payload = parser.parse(testData)

        assertThat(payload).isNull()
    }

}
