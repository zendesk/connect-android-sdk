package com.zendesk.connect

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.willReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class IntentBuilderTests {

    private val testAction = "some_action"
    private val testUrl = "some_url"
    private val testFlags = 1 or 2
    private val testPackageName = "some.package.name"
    private val testName = "some_intent_key"
    private val testClassName = "com.org.net"

    private val mockIntent = mock<Intent>()
    private val mockUri = mock<Uri>()
    private val mockContext = mock<Context>()
    private val mockParcelable = mock<Parcelable>()

    private lateinit var spyIntentBuilder: IntentBuilder

    @Before
    fun setUp() {
        spyIntentBuilder = spy(IntentBuilder(mockIntent))

        willReturn(mockUri).given(spyIntentBuilder).parseUrl(anyString())
    }

    @Test
    fun `from should set the initial intent of a new builder`() {
        val anotherIntent = mock<Intent>()
        val newBuilder = spyIntentBuilder.from(anotherIntent)

        assertThat(newBuilder.initialIntent).isEqualTo(anotherIntent)
    }

    @Test
    fun `from should not change the initial intent of the builder`() {
        val anotherIntent = mock<Intent>()
        spyIntentBuilder.from(anotherIntent)

        assertThat(spyIntentBuilder.initialIntent).isEqualTo(mockIntent)
    }

    @Test
    fun `from should set the builder intent of a new builder`() {
        val anotherIntent = mock<Intent>()
        val newBuilder = spyIntentBuilder.from(anotherIntent)

        assertThat(newBuilder.builderIntent).isEqualTo(anotherIntent)
    }

    @Test
    fun `from should not change the builder intent of the builder`() {
        val anotherIntent = mock<Intent>()
        spyIntentBuilder.from(anotherIntent)

        assertThat(spyIntentBuilder.builderIntent).isEqualTo(mockIntent)
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
    fun `withClassName should set the class for the intent`() {
        spyIntentBuilder.withClassName(mockContext, testClassName)

        verify(mockIntent).setClassName(mockContext, testClassName)
    }

    @Test
    fun `withExtra parcelable should set the parcelable extra for the intent`() {
        spyIntentBuilder.withExtra(testName, mockParcelable)

        verify(mockIntent).putExtra(testName, mockParcelable)
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
}
