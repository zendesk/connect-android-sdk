package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EventFactoryTests {

    @Test
    fun `create event should create an event object with the given name`() {
        val eventName = "The Gang Makes Lethal Weapon 6"

        val event = EventFactory.createEvent(eventName)

        assertThat(event.event).isEqualTo(eventName)
    }

    @Test
    fun `create event should timestamp the event`() {
        val event = EventFactory.createEvent("The Gang Dines Out")

        assertThat(event.timestamp).isNotNull()
    }

    @Test
    fun `create event with no properties should create an event object with null properties`() {
        val event = EventFactory.createEvent("The Gang Tries Desperately to Win an Award")

        assertThat(event.properties).isNull()
    }

    @Test
    fun `create event with properties should create an event object with those properties`() {
        val eventProperties = mapOf(Pair("Frank", "Hot Dog"), Pair("Mac", "Kung Fu"))

        val event = EventFactory.createEvent("The Gang Saves the Day", eventProperties)

        assertThat(event.properties).isEqualTo(eventProperties)
    }
}