package com.zendesk.connect

import android.app.Notification
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.support.v4.app.NotificationCompat
import com.google.common.truth.Truth.assertThat
import com.zendesk.logger.Logger
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class NotificationBuilderTests {

    companion object {
        private const val LOG_SMALL_ICON_DOESNT_EXIST = "Small icon doesn't exist, using default icon"
    }

    private val testNotificationChannelId = "test_channel_id"
    private val testTitle = "Mac & Dennis Move to the Suburbs"
    private val testBody = "You ever been been in a storm Wally?"
    private val testIconResourceId = 115
    private val testFallbackIconResourceId = 230
    private val testIconFileName = "Mac's Famous Mac and Cheese"
    private val testIconFolder = "Boxes in the Pantry"
    private val testPackageName = "Muscle Mac"
    private val testCategory = "Disturbing"

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    private lateinit var spyNotificationBuilder: NotificationBuilder

    @Mock
    private lateinit var mockCompatBuilder: NotificationCompat.Builder

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockResources: Resources

    @Mock
    private lateinit var mockStyle: NotificationCompat.Style

    @Mock
    private lateinit var mockBitmap: Bitmap

    @Mock
    private lateinit var mockNotification: Notification

    @Before
    fun setUp() {
        `when`(mockContext.resources).thenReturn(mockResources)
        `when`(mockContext.packageName).thenReturn(testPackageName)

        `when`(mockResources.getIdentifier(testIconFileName, testIconFolder, testPackageName))
                .thenReturn(testIconResourceId)

        `when`(mockCompatBuilder.build()).thenReturn(mockNotification)

        spyNotificationBuilder = spy(NotificationBuilder(
                mockCompatBuilder,
                mockContext,
                testNotificationChannelId
        ))

        doReturn(mockBitmap).`when`(spyNotificationBuilder).decodeLargeIconResource(mockResources, testIconResourceId)
    }

    @Test
    fun `setTitle should set the title attribute on the builder`() {
        spyNotificationBuilder.setTitle(testTitle)

        verify(mockCompatBuilder).setContentTitle(testTitle)
    }

    @Test
    fun `setBody should set the body attribute on the builder`() {
        spyNotificationBuilder.setBody(testBody)

        verify(mockCompatBuilder).setContentText(testBody)
    }

    @Test
    fun `setStyle should set the style attribute on the builder`() {
        spyNotificationBuilder.setStyle(mockStyle)

        verify(mockCompatBuilder).setStyle(mockStyle)
    }

    @Test
    fun `setAutoCancel should set the auto cancel attribute on the builder`() {
        spyNotificationBuilder.setAutoCancel(true)

        verify(mockCompatBuilder).setAutoCancel(true)
    }

    @Test
    fun `setLocalOnly should set the local only attribute on the builder`() {
        spyNotificationBuilder.setLocalOnly(true)

        verify(mockCompatBuilder).setLocalOnly(true)
    }

    @Test
    fun `setSmallIcon via identifier should set the small icon attribute on the builder`() {
        spyNotificationBuilder.setSmallIcon(testIconResourceId)

        verify(mockCompatBuilder).setSmallIcon(testIconResourceId)
    }

    @Test
    fun `setSmallIcon via filename should search for the desired resource`() {
        spyNotificationBuilder.setSmallIcon(testIconFileName, testIconFolder, testFallbackIconResourceId)

        verify(mockResources).getIdentifier(testIconFileName, testIconFolder, testPackageName)
    }

    @Test
    fun `setSmallIcon via filename should set the small icon attribute on the builder`() {
        spyNotificationBuilder.setSmallIcon(testIconFileName, testIconFolder, testFallbackIconResourceId)

        verify(mockCompatBuilder).setSmallIcon(testIconResourceId)
    }

    @Test
    fun `setSmallIcon via filename should use the fallback icon id if getIdentifier returns 0`() {
        `when`(mockResources.getIdentifier(testIconFileName, testIconFolder, testPackageName))
                .thenReturn(0)

        spyNotificationBuilder.setSmallIcon(testIconFileName, testIconFolder, testFallbackIconResourceId)

        verify(mockCompatBuilder).setSmallIcon(testFallbackIconResourceId)
    }

    @Test
    fun `setSmallIcon via filename should log a warning if getIdentifier returns 0`() {
        `when`(mockResources.getIdentifier(testIconFileName, testIconFolder, testPackageName))
                .thenReturn(0)

        spyNotificationBuilder.setSmallIcon(testIconFileName, testIconFolder, testFallbackIconResourceId)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_SMALL_ICON_DOESNT_EXIST)
    }

    @Test
    fun `setLargeIcon via bitmap should set the large icon attribute on the builder`() {
        spyNotificationBuilder.setLargeIcon(mockBitmap)

        verify(mockCompatBuilder).setLargeIcon(mockBitmap)
    }

    @Test
    fun `setLargeIcon via filename should search for the desired resource`() {
        spyNotificationBuilder.setLargeIcon(testIconFileName, testIconFolder)

        verify(mockResources).getIdentifier(testIconFileName, testIconFolder, testPackageName)
    }

    @Test
    fun `setSilent should set the sound as null on the builder`() {
        spyNotificationBuilder.setSilent()

        verify(mockCompatBuilder).setSound(null)
    }

    @Test
    fun `setSilent should set the channel id on the builder`() {
        spyNotificationBuilder.setSilent()

        verify(mockCompatBuilder).setChannelId(testNotificationChannelId)
    }

    @Test
    fun `setCategory should set the category attribute on the builder`() {
        spyNotificationBuilder.setCategory(testCategory)

        verify(mockCompatBuilder).setCategory(testCategory)
    }

    @Test
    fun `build should return a non null object`() {
        val output = spyNotificationBuilder.build()

        assertThat(output).isNotNull()
    }

    @Test
    fun `build should return an instance of Notification`() {
        val output = spyNotificationBuilder.build()

        assertThat(output).isInstanceOf(Notification::class.java)
    }
}
