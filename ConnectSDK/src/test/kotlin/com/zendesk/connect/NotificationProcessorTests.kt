package com.zendesk.connect

import android.graphics.Bitmap
import android.support.v4.app.NotificationCompat
import com.google.common.truth.Truth.assertThat
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.zendesk.logger.Logger
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class NotificationProcessorTests {

    private val NULL_PAYLOAD_DATA = "Payload data was null, unable to create notification"
    private val NO_SMALL_ICON = "Small icon doesn't exist, using default icon"
    private val NO_LARGE_ICON = "Large icon doesn't exist, there will be no large icon"
    private val NULL_REMOTE_MESSAGE = "Notification data was null or empty"

    private val gson = Gson()

    private val testTitle = "wubalub"
    private val testBody = "Grass tastes bad"
    private lateinit var testData: MutableMap<String, String>

    private lateinit var notificationProcessor: NotificationProcessor

    @Mock
    private lateinit var mockNotificationBuilder: NotificationBuilder

    @Mock
    private lateinit var mockRemoteMessage: RemoteMessage

    @Mock
    private lateinit var mockPayload: NotificationPayload

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    @Before
    fun setUp() {
        testData = mutableMapOf(
                Pair("title", testTitle),
                Pair("body", testBody)
        )

        MockitoAnnotations.initMocks(NotificationProcessorTests::class.java)

        `when`(mockRemoteMessage.data).thenReturn(testData)
        `when`(mockNotificationBuilder.setTitle(anyString())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setBody(anyString())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setStyle(any<NotificationCompat.Style>())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setAutoCancel(anyBoolean())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setLocalOnly(anyBoolean())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setSmallIcon(anyInt())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setSmallIcon(anyString(), anyString())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setLargeIcon(any<Bitmap>())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setLargeIcon(anyString(), anyString())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setCategory(anyString())).thenReturn(mockNotificationBuilder)

        notificationProcessor = NotificationProcessor(gson, mockNotificationBuilder)
    }

    @Test
    fun `build connect notification should return null if notification payload is null`() {
        val notification = notificationProcessor.buildConnectNotification(null)

        assertThat(notification).isNull()
    }

    @Test
    fun `build connect notification should log warning if notification payload is null`() {
        notificationProcessor.buildConnectNotification(null)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_PAYLOAD_DATA)
    }

    @Test
    fun `build connect notification should add a title to the notification builder` () {
        `when`(mockPayload.title).thenReturn(testTitle)

        notificationProcessor.buildConnectNotification(mockPayload)

        verify(mockNotificationBuilder).setTitle(testTitle)
    }

    @Test
    fun `build connect notification should add a body to the notification builder` () {
        `when`(mockPayload.body).thenReturn(testBody)

        notificationProcessor.buildConnectNotification(mockPayload)

        verify(mockNotificationBuilder).setBody(testBody)
    }

    @Test
    fun `build connect notification should set auto cancel to some value`() {
        notificationProcessor.buildConnectNotification(mockPayload)

        verify(mockNotificationBuilder).setAutoCancel(anyBoolean())
    }

    @Test
    fun `build connect notification should set local only to some value`() {
        notificationProcessor.buildConnectNotification(mockPayload)

        verify(mockNotificationBuilder).setLocalOnly(anyBoolean())
    }

    @Test
    fun `build connect notification should set the notification style`() {
        `when`(mockPayload.body).thenReturn("some message")

        notificationProcessor.buildConnectNotification(mockPayload)

        verify(mockNotificationBuilder).setStyle(any<NotificationCompat.Style>())
    }

    @Test
    fun `build connect notification should set a default small icon if not specified`() {
        notificationProcessor.buildConnectNotification(mockPayload)

        verify(mockNotificationBuilder).setSmallIcon(R.drawable.ic_connect_notification_icon)
    }

    @Test
    fun `build connect notification should log a warning if small icon not specified`() {
        notificationProcessor.buildConnectNotification(mockPayload)

        assertThat(logAppender.logs).contains(NO_SMALL_ICON)
    }

    @Test
    fun `build connect notification should set a small icon with the file and folder specified`() {
        val smallImageFile = "tiny_image"
        val smallFolderFile = "miniscule_folder"
        `when`(mockPayload.smallNotificationImagePath).thenReturn(smallImageFile)
        `when`(mockPayload.smallNotificationFolderPath).thenReturn(smallFolderFile)

        notificationProcessor.buildConnectNotification(mockPayload)

        verify(mockNotificationBuilder).setSmallIcon(smallImageFile, smallFolderFile)
    }

    @Test
    fun `build connect notification should not set a large icon if not specified`() {
        notificationProcessor.buildConnectNotification(mockPayload)

        verify(mockNotificationBuilder, never()).setLargeIcon(any<Bitmap>())
    }

    @Test
    fun `build connect notification should log a warning if large icon not specified`() {
        notificationProcessor.buildConnectNotification(mockPayload)

        assertThat(logAppender.logs).contains(NO_LARGE_ICON)
    }

    @Test
    fun `build connect notification should set a large icon with the file and folder specified`() {
        val largeImageFile = "huge_image"
        val largeFolderFile = "massive_folder"
        `when`(mockPayload.largeNotificationImagePath).thenReturn(largeImageFile)
        `when`(mockPayload.largeNotificationFolderPath).thenReturn(largeFolderFile)

        notificationProcessor.buildConnectNotification(mockPayload)

        verify(mockNotificationBuilder).setLargeIcon(largeImageFile, largeFolderFile)
    }

    @Test
    fun `build connect notification should not set notification category if not specified`() {
        notificationProcessor.buildConnectNotification(mockPayload)

        verify(mockNotificationBuilder, never()).setCategory(anyString())
    }

    @Test
    fun `build connect notification should set notification category if specified`() {
        val category = "fake news"
        `when`(mockPayload.category).thenReturn(category)

        notificationProcessor.buildConnectNotification(mockPayload)

        verify(mockNotificationBuilder).setCategory(category)
    }

    @Test
    fun `build connect notification should set a pending intent for the notification`() {
        notificationProcessor.buildConnectNotification(mockPayload)

        verify(mockNotificationBuilder).setPendingIntent(mockPayload)
    }

    @Test
    fun `parse remote message should log a warning for null remote message`() {
        notificationProcessor.parseRemoteMessage(null)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_REMOTE_MESSAGE)
    }

    @Test
    fun `parse remote message should return null for null remote message`() {
        val parsedMessage = notificationProcessor.parseRemoteMessage(null)

        assertThat(parsedMessage).isNull()
    }

    @Test
    fun `parse remote message should log a warning for null remote message data`() {
        `when`(mockRemoteMessage.data).thenReturn(null)

        notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_REMOTE_MESSAGE)
    }

    @Test
    fun `parse remote message should return null for null remote message data`() {
        `when`(mockRemoteMessage.data).thenReturn(null)

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage).isNull()
    }

    @Test
    fun `parsed remote message should contain the message title`() {
        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.title).isEqualTo(testTitle)
    }

    @Test
    fun `parsed remote message should contain the message body`() {
        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.body).isEqualTo(testBody)
    }

    @Test
    fun `parsed remote message should contain the message notification id`() {
        val notificationId = 42
        testData["_onid"] = "$notificationId"

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.notificationId).isEqualTo(notificationId)
    }

    @Test
    fun `parsed remote message should contain the quiet push flag from the message payload`() {
        val isQuietPush = true
        testData["_oq"] = "$isQuietPush"

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.isQuietPush).isEqualTo(isQuietPush)
    }

    @Test
    fun `parsed remote message should contain the uninstall tracker flag from the message payload`() {
        val isUninstallTracker = true
        testData["_ogp"] = "$isUninstallTracker"

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.isUninstallTracker).isEqualTo(isUninstallTracker)
    }

    @Test
    fun `parsed remote message should contain the test push flag from the message payload`() {
        val isTestPush = true
        testData["_otm"] = "$isTestPush"

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.isTestPush).isEqualTo(isTestPush)
    }

    @Test
    fun `parsed remote message should contain the silent flag from the message payload`() {
        val isSilent = true
        testData["_silent"] = "$isSilent"

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.isSilent).isEqualTo(isSilent)
    }

    @Test
    fun `parsed remote message should contain the default sound flag from the message payload`() {
        val isDefaultSound = true
        testData["_soundDefault"] = "$isDefaultSound"

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.isDefaultSound).isEqualTo(isDefaultSound)
    }

    @Test
    fun `parsed remote message should contain the message payload`() {
        val key = "bonus"
        val value = "5 points"
        testData["payload"] = "{\"$key\":\"$value\"}"

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.payload[key]).isEqualTo(value)
    }

    @Test
    fun `parsed remote message should contain the message instance id`() {
        val instanceId = "some_id"
        testData["_oid"] = instanceId

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.instanceId).isEqualTo(instanceId)
    }

    @Test
    fun `parsed remote message should contain the message deeplink url`() {
        val deeplinkUrl = "some_awesome_article_or_something.com/backslash@yahoo.com"
        testData["_odl"] = deeplinkUrl

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.deeplinkUrl).isEqualTo(deeplinkUrl)
    }

    @Test
    fun `parsed remote message should contain the message category`() {
        val category = "fake news"
        testData["category"] = category

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.category).isEqualTo(category)
    }

    @Test
    fun `parsed remote message should contain the message large icon file name`() {
        val largeIconFileName = "big_image"
        testData["_lni"] = largeIconFileName

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.largeNotificationImagePath).isEqualTo(largeIconFileName)
    }

    @Test
    fun `parsed remote message should contain the message large icon folder name`() {
        val largeIconFolderName = "big_folder"
        testData["_lnf"] = largeIconFolderName

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.largeNotificationFolderPath).isEqualTo(largeIconFolderName)
    }

    @Test
    fun `parsed remote message should contain the message small icon file name`() {
        val smallIconFileName = "little_image"
        testData["_sni"] = smallIconFileName

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.smallNotificationImagePath).isEqualTo(smallIconFileName)
    }

    @Test
    fun `parsed remote message should contain the message small icon folder name`() {
        val smallIconFolderName = "little_folder"
        testData["_snf"] = smallIconFolderName

        val parsedMessage = notificationProcessor.parseRemoteMessage(mockRemoteMessage)

        assertThat(parsedMessage.smallNotificationFolderPath).isEqualTo(smallIconFolderName)
    }

    @After
    fun tearDown() {

    }

}