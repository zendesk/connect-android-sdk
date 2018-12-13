package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import java.io.*

class GsonConverterTests {

    data class Episode(val title: String)

    private lateinit var gsonConverter: GsonConverter<Episode>

    private lateinit var outputStream: ByteArrayOutputStream
    private val gson = Gson()

    @Before
    fun setUp() {
        gsonConverter = GsonConverter(gson, Episode::class.java)
        outputStream = ByteArrayOutputStream()
    }

    @Test
    fun `from should transform the given byte array into the specified type`() {
        val episode = Episode("The Gang Misses the Boat")

        val bytes = gson.toJson(episode).toByteArray()

        assertThat(gsonConverter.from(bytes)).isEqualTo(episode)
    }

    @Test
    fun `to stream should write the given object to the given output stream`() {
        val episode = Episode("The Gang Saves the Day")

        gsonConverter.toStream(episode, outputStream)

        assertThat(outputStream.toString()).isEqualTo(gson.toJson(episode))
    }

    @Test(expected = IOException::class)
    fun `to stream should throw an exception if a problem is encountered in the output stream`() {
        val episode = Episode("The Gang gets Quarantined")

        val stream = FileOutputStream(File("")) // no file here

        gsonConverter.toStream(episode, stream)
    }

}

