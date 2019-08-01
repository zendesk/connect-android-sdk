package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.spy
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class SystemPushPayloadParserTests {

    private lateinit var parser: SystemPushPayloadParser
    private lateinit var testData: MutableMap<String, String>

    private val gson = Gson()
    private val data = mapOf(
            "_oq" to "true",
            "_ogp" to "true",
            "_otm" to "true",
            "_silent" to "true",
            "_soundDefault" to "true",
            "_onid" to "23",
            "_oid" to "c137",
            "_odl" to "www.RickAndMortyAdventuresAhHundredYearsEveryMinuteRickAndMorty.com",
            "title" to "",
            "body" to "",
            "category" to "grim",
            "_lni" to "jpeg.png",
            "_lnf" to ">dev/null",
            "_sni" to "png.gif",
            "_snf" to ">dev/nuller"
    )

    @Before
    fun setUp() {
        parser = spy(SystemPushPayloadParser(gson))
        testData = HashMap(data)
    }

    @Test
    fun `parse should return a non null instance of SystemPushPayload`() {
        val payload = parser.parse(testData)

        assertThat(payload).isNotNull()
    }
    
    @Test
    fun `parse should populate the model with the received quiet push value`() {
        val payload = parser.parse(testData)

        assertThat(payload.isQuietPush).isEqualTo(data.getValue("_oq").toBoolean())
    }
    
    @Test
    fun `parse should populate the model with the received uninstall tracker value`() {
        val payload = parser.parse(testData)

        assertThat(payload.isUninstallTracker).isEqualTo(data.getValue("_ogp").toBoolean())
    }
    
    @Test
    fun `parse should populate the model with the received test push value`() {
        val payload = parser.parse(testData)

        assertThat(payload.isTestPush).isEqualTo(data.getValue("_otm").toBoolean())
    }

    @Test
    fun `parse should populate the model with the received is silent value`() {
        val payload = parser.parse(testData)

        assertThat(payload.isSilent).isEqualTo(data.getValue("_silent").toBoolean())
    }

    @Test
    fun `parse should populate the model with the received sound default value`() {
        val payload = parser.parse(testData)

        assertThat(payload.isDefaultSound).isEqualTo(data.getValue("_soundDefault").toBoolean())
    }

    @Test
    fun `parse should populate the model with the received notification id`() {
        val payload = parser.parse(testData)

        assertThat(payload.notificationId).isEqualTo(data.getValue("_onid").toInt())
    }

    @Test
    fun `parse should populate the model with the received instance id`() {
        val payload = parser.parse(testData)

        assertThat(payload.instanceId).isEqualTo(data["_oid"])
    }

    @Test
    fun `parse should populate the model with the received deep link url`() {
        val payload = parser.parse(testData)

        assertThat(payload.deeplinkUrl).isEqualTo(data["_odl"])
    }

    @Test
    fun `parse should populate the model with the received title value`() {
        val payload = parser.parse(testData)

        assertThat(payload.title).isEqualTo(data["title"])
    }

    @Test
    fun `parse should populate the model with the received body value`() {
        val payload = parser.parse(testData)

        assertThat(payload.body).isEqualTo(data["body"])
    }

    @Test
    fun `parse should populate the model with the received category value`() {
        val payload = parser.parse(testData)

        assertThat(payload.category).isEqualTo(data["category"])
    }

    @Test
    fun `parse should populate the model with the received large notification image path`() {
        val payload = parser.parse(testData)

        assertThat(payload.largeNotificationImagePath).isEqualTo(data["_lni"])
    }

    @Test
    fun `parse should populate the model with the received large notification file path`() {
        val payload = parser.parse(testData)

        assertThat(payload.largeNotificationFolderPath).isEqualTo(data["_lnf"])
    }

    @Test
    fun `parse should populate the model with the received small notification image path`() {
        val payload = parser.parse(testData)

        assertThat(payload.smallNotificationImagePath).isEqualTo(data["_sni"])
    }

    @Test
    fun `parse should populate the model with the received small notification file path`() {
        val payload = parser.parse(testData)

        assertThat(payload.smallNotificationFolderPath).isEqualTo(data["_snf"])
    }

    @Test
    fun `parse should place any unknown keys into a custom properties map`() {
        val customPair1 = "some_action" to "intent_url_or_something"
        testData.putAll(mapOf(customPair1))
        val payload = parser.parse(testData)

        assertThat(payload.payload).containsEntry(customPair1.first, customPair1.second)
    }

}