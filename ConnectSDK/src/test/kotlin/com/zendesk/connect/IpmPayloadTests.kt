package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IpmPayloadTests {

    private val instanceId = "some_instance_id"
    private val timeToLive = 42L
    private val logo = ".././..//.././//./.../././//./picture.txt"
    private val heading = "(x_x)"
    private val message = "-<-<"
    private val buttonText = "Don't push me"
    private val action = "go to jail, don't pass go"
    private val headingFontColor = "#000000"
    private val messageFontColor = "#ff0000"
    private val backgroundColor = "#00ff00"
    private val buttonBackgroundColor = "#0000ff"
    private val buttonTextColor = "#ffffff"
    private val ipmPayload = IpmPayload(
        instanceId,
        timeToLive,
        logo,
        heading,
        message,
        buttonText,
        action,
        headingFontColor,
        messageFontColor,
        backgroundColor,
        buttonBackgroundColor,
        buttonTextColor
    )

    @Test
    fun `getInstanceId should return the instance id`() {
        assertThat(ipmPayload.instanceId).isEqualTo(instanceId)
    }

    @Test
    fun `getTimeToLive should return the time to live`() {
        assertThat(ipmPayload.timeToLive).isEqualTo(timeToLive.toLong())
    }

    @Test
    fun `getLogo should return the source of the avatar logo`() {
        assertThat(ipmPayload.logo).isEqualTo(logo)
    }

    @Test
    fun `getHeading should return the heading text`() {
        assertThat(ipmPayload.heading).isEqualTo(heading)
    }

    @Test
    fun `getMessage should return the message text`() {
        assertThat(ipmPayload.message).isEqualTo(message)
    }

    @Test
    fun `getButtonText should return the text for the button`() {
        assertThat(ipmPayload.buttonText).isEqualTo(buttonText)
    }

    @Test
    fun `getAction should return the action for the button`() {
        assertThat(ipmPayload.action).isEqualTo(action)
    }

    @Test
    fun `getHeadingFontColor should return the font color for the heading`() {
        assertThat(ipmPayload.headingFontColor).isEqualTo(headingFontColor)
    }

    @Test
    fun `getMessageFontColor should return the font color for the message`() {
        assertThat(ipmPayload.messageFontColor).isEqualTo(messageFontColor)
    }

    @Test
    fun `getBackgroundColor should return the background color`() {
        assertThat(ipmPayload.backgroundColor).isEqualTo(backgroundColor)
    }

    @Test
    fun `getButtonBackgroundColor should return the button background color`() {
        assertThat(ipmPayload.buttonBackgroundColor).isEqualTo(buttonBackgroundColor)
    }

    @Test
    fun `getButtonTextColor should return the button text color`() {
        assertThat(ipmPayload.buttonTextColor).isEqualTo(buttonTextColor)
    }

}
