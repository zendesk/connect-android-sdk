package com.zendesk.connect

import android.content.Intent
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class IntentBuilderTests {

    private val testAction = "some_action"
    private val testUrl = "some_url"
    private val testFlags = 1 or 2
    private val testPackageName = "some.package.name"

    private lateinit var spyIntentBuilder: IntentBuilder

    @Mock
    private lateinit var mockIntent: Intent

    @Mock
    private lateinit var mockUri: Uri

    @Before
    fun setUp() {
        spyIntentBuilder = spy(IntentBuilder(mockIntent))

        doReturn(mockUri).`when`(spyIntentBuilder).parseUrl(anyString())
    }

    @Test
    fun `withAction should set the action on the intent`() {
        spyIntentBuilder.withAction(testAction)

        verify(mockIntent).action = testAction
    }

    @Test
    fun `withData via Uri should set the Uri data on the intent`() {
        spyIntentBuilder.withData(mockUri)

        verify(mockIntent).data = mockUri
    }

    @Test
    fun `withData via String should parse the url as a Uri`() {
        spyIntentBuilder.withData(testUrl)

        verify(spyIntentBuilder).parseUrl(testUrl)
    }

    @Test
    fun `withData via String should set the parsed Uri data on the intent`() {
        spyIntentBuilder.withData(testUrl)

        verify(mockIntent).data = mockUri
    }

    @Test
    fun `withFlags should set the flags on the intent`() {
        spyIntentBuilder.withFlags(testFlags)

        verify(mockIntent).flags = testFlags
    }

    @Test
    fun `withPackageName should set the package name on the intent`() {
        spyIntentBuilder.withPackageName(testPackageName)

        verify(mockIntent).`package` = testPackageName
    }

    @Test
    fun `build should return a non null intent`() {
        val output = spyIntentBuilder.build()

        assertThat(output).isNotNull()
    }

    @Test
    fun `build should return an instance of an Intent`() {
        val output = spyIntentBuilder.build()

        assertThat(output).isInstanceOf(Intent::class.java)
    }

    @After
    fun tearDown() {

    }
}