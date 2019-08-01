package com.zendesk.connect

import android.content.Context
import android.content.Intent
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class NavigatorTests {

    private lateinit var navigator: Navigator

    private val mockContext = mock<Context>()
    private val mockIntentBuilder = mock<IntentBuilder>()
    private val mockIntent = mock<Intent>()

    @Before
    fun setUp() {
        navigator = Navigator(mockIntentBuilder)

        `when`(mockIntentBuilder.build()).thenReturn(mockIntent)
    }

    @Test
    fun `startIpmActivity should add the IpmActivity to the intent builder`() {
        navigator.startIpmActivity(mockContext)

        verify(mockIntentBuilder).withClassName(mockContext, IpmActivity::class.java.name)
    }

    @Test
    fun `startIpmActivity should add a new activity flag to the intent builder`() {
        navigator.startIpmActivity(mockContext)

        verify(mockIntentBuilder).withFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    
    @Test
    fun `startIpmActivity should start the activity with the constructed intent`() {
        navigator.startIpmActivity(mockContext)

        verify(mockContext).startActivity(mockIntent)
    }

}
