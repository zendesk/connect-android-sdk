package com.zendesk.connect

import android.app.Notification
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import com.google.common.truth.Truth.assertThat
import com.zendesk.logger.Logger
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class SystemPushStrategyTests {

    companion object {
        private const val NULL_PAYLOAD_DATA = "Payload data was null, unable to create notification"
        private const val NO_LARGE_ICON = "Large icon doesn't exist, there will be no large icon"
        private const val UNABLE_TO_BUILD_NOTIFICATION = "Unable to build notification to display"
        private const val NULL_METRICS_PROCESSOR = "Metrics processor was null, unable to send metrics"
    }

    private val testTitle = "wubalub"
    private val testBody = "Grass tastes bad"
    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    private val mockManager = mock<NotificationManager>()
    private val mockBuilder = mock<NotificationBuilder>()
    private val mockMetricsProcessor = mock<MetricRequestsProcessor>()
    private val mockEventListener = mock<NotificationEventListener>()
    private val mockNotificationFactory = mock<NotificationFactory>()
    private val mockNotification = mock<Notification>()
    private val mockPayload = mock<SystemPushPayload>()
    private val mockParser = mock<SystemPushPayloadParser>()

    private lateinit var pushStrategy: SystemPushStrategy
    private lateinit var testData: MutableMap<String, String>

    @Before
    fun setUp() {

        `when`(mockBuilder.setTitle(anyString())).thenReturn(mockBuilder)
        `when`(mockBuilder.setBody(anyString())).thenReturn(mockBuilder)
        `when`(mockBuilder.setStyle(any<NotificationCompat.Style>())).thenReturn(mockBuilder)
        `when`(mockBuilder.setAutoCancel(anyBoolean())).thenReturn(mockBuilder)
        `when`(mockBuilder.setLocalOnly(anyBoolean())).thenReturn(mockBuilder)
        `when`(mockBuilder.setSmallIcon(anyInt())).thenReturn(mockBuilder)
        `when`(mockBuilder.setSmallIcon(anyString(), anyString(), anyInt())).thenReturn(mockBuilder)
        `when`(mockBuilder.setLargeIcon(any<Bitmap>())).thenReturn(mockBuilder)
        `when`(mockBuilder.setLargeIcon(anyString(), anyString())).thenReturn(mockBuilder)
        `when`(mockBuilder.setSilent()).thenReturn(mockBuilder)
        `when`(mockBuilder.setCategory(anyString())).thenReturn(mockBuilder)
        `when`(mockBuilder.build()).thenReturn(mockNotification)
        `when`(mockParser.parse(any<Map<String, String>>())).thenReturn(mockPayload)

        testData = mutableMapOf(
                "title" to testTitle,
                "body" to testBody
        )

        pushStrategy = spy(SystemPushStrategy(
                mockManager,
                mockBuilder,
                mockMetricsProcessor,
                mockEventListener,
                mockNotificationFactory,
                mockParser
        ))
    }

    @Test
    fun `process should parse the notification payload from the data given`() {
        pushStrategy.process(testData)

        verify(mockParser).parse(any<Map<String, String>>())
    }

    @Test
    fun `process should display the parsed notification`() {
        pushStrategy.process(testData)

        verify(pushStrategy).displayNotification(any<SystemPushPayload>())
    }

    @Test
    fun `process should not display the notification if a quiet push is received`() {
        `when`(mockPayload.isQuietPush).thenReturn(true)

        pushStrategy.process(testData)

        verify(pushStrategy, never())
                .displayNotification(mockPayload)
    }

    @Test
    fun `process should send metrics for the notification`() {
        pushStrategy.process(testData)

        verify(pushStrategy).sendMetrics(
                any<SystemPushPayload>(),
                any<MetricRequestsProcessor>(),
                any<NotificationManager>()
        )
    }

    @Test
    fun `process should call the onNotificationReceived event listener`() {
        pushStrategy.process(testData)

        verify(mockEventListener).onNotificationReceived(any<SystemPushPayload>())
    }

    @Test
    fun `build connect notification should return null if notification payload is null`() {
        val notification = pushStrategy.buildConnectNotification(null, mockBuilder)

        assertThat(notification).isNull()
    }

    @Test
    fun `build connect notification should log warning if notification payload is null`() {
        pushStrategy.buildConnectNotification(null, mockBuilder)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_PAYLOAD_DATA)
    }

    @Test
    fun `build connect notification should add a title to the notification builder`() {
        `when`(mockPayload.title).thenReturn(testTitle)

        pushStrategy.buildConnectNotification(mockPayload, mockBuilder)

        verify(mockBuilder).setTitle(testTitle)
    }

    @Test
    fun `build connect notification should add a body to the notification builder`() {
        `when`(mockPayload.body).thenReturn(testBody)

        pushStrategy.buildConnectNotification(mockPayload, mockBuilder)

        verify(mockBuilder).setBody(testBody)
    }

    @Test
    fun `build connect notification should set auto cancel to some value`() {
        pushStrategy.buildConnectNotification(mockPayload, mockBuilder)

        verify(mockBuilder).setAutoCancel(anyBoolean())
    }

    @Test
    fun `build connect notification should set local only to some value`() {
        pushStrategy.buildConnectNotification(mockPayload, mockBuilder)

        verify(mockBuilder).setLocalOnly(anyBoolean())
    }

    @Test
    fun `build connect notification should set the notification style`() {
        `when`(mockPayload.body).thenReturn("some message")

        pushStrategy.buildConnectNotification(mockPayload, mockBuilder)

        verify(mockBuilder).setStyle(any<NotificationCompat.Style>())
    }

    @Test
    fun `build connect notification should set a small icon with the file and folder specified and fallback icon id`() {
        val smallImageFile = "tiny_image"
        val smallFolderFile = "miniscule_folder"
        `when`(mockPayload.smallNotificationImagePath).thenReturn(smallImageFile)
        `when`(mockPayload.smallNotificationFolderPath).thenReturn(smallFolderFile)

        pushStrategy.buildConnectNotification(mockPayload, mockBuilder)

        verify(mockBuilder).setSmallIcon(smallImageFile, smallFolderFile, R.drawable.ic_connect_notification_icon)
    }

    @Test
    fun `build connect notification should not set a large icon if not specified`() {
        pushStrategy.buildConnectNotification(mockPayload, mockBuilder)

        verify(mockBuilder, never()).setLargeIcon(any<Bitmap>())
    }

    @Test
    fun `build connect notification should log a warning if large icon not specified`() {
        pushStrategy.buildConnectNotification(mockPayload, mockBuilder)

        assertThat(logAppender.logs).contains(NO_LARGE_ICON)
    }

    @Test
    fun `build connect notification should set a large icon with the file and folder specified`() {
        val largeImageFile = "huge_image"
        val largeFolderFile = "massive_folder"
        `when`(mockPayload.largeNotificationImagePath).thenReturn(largeImageFile)
        `when`(mockPayload.largeNotificationFolderPath).thenReturn(largeFolderFile)

        pushStrategy.buildConnectNotification(mockPayload, mockBuilder)

        verify(mockBuilder).setLargeIcon(largeImageFile, largeFolderFile)
    }

    // region setSilent
    @Test
    fun `build connect notification should not set silent if payload isSilent is false`() {
        given(mockPayload.isSilent).willReturn(false)

        pushStrategy.buildConnectNotification(mockPayload, mockBuilder)

        verify(mockBuilder, never()).setSilent()
    }

    @Test
    fun `build connect notification should set silent if payload value is true`() {
        given(mockPayload.isSilent).willReturn(true)

        pushStrategy.buildConnectNotification(mockPayload, mockBuilder)

        verify(mockBuilder).setSilent()
    }
    // endregion

    @Test
    fun `build connect notification should not set notification category if not specified`() {
        pushStrategy.buildConnectNotification(mockPayload, mockBuilder)

        verify(mockBuilder, never()).setCategory(anyString())
    }

    @Test
    fun `build connect notification should set notification category if specified`() {
        val category = "fake news"
        `when`(mockPayload.category).thenReturn(category)

        pushStrategy.buildConnectNotification(mockPayload, mockBuilder)

        verify(mockBuilder).setCategory(category)
    }

    @Test
    fun `build connect notification should set a pending intent for the notification`() {
        pushStrategy.buildConnectNotification(mockPayload, mockBuilder)

        verify(mockBuilder).setPendingIntent(mockPayload)
    }

    @Test
    fun `display notification should attempt to retrieve the integrator custom notification display`() {
        pushStrategy.displayNotification(mockPayload)

        verify(mockNotificationFactory).create(mockPayload)
    }

    @Test
    fun `display notification should build a connect display notification if none provided by the integrator`() {
        `when`(mockNotificationFactory.create(mockPayload)).thenReturn(null)

        pushStrategy.displayNotification(mockPayload)

        verify(pushStrategy).buildConnectNotification(mockPayload, mockBuilder)
    }

    @Test
    fun `display notification should log an error if the display notification is null`() {
        `when`(mockNotificationFactory.create(mockPayload)).thenReturn(null)
        `when`(pushStrategy.buildConnectNotification(mockPayload, mockBuilder)).thenReturn(null)

        pushStrategy.displayNotification(mockPayload)

        assertThat(logAppender.lastLog()).isEqualTo(UNABLE_TO_BUILD_NOTIFICATION)
    }

    @Test
    fun `display notification should display the notification if it was not null`() {
        `when`(mockNotificationFactory.create(mockPayload)).thenReturn(null)

        pushStrategy.displayNotification(mockPayload)

        verify(mockManager).notify(mockPayload.notificationId, mockNotification)
    }

    @Test
    fun `send metrics should log a warning if metrics processor is null`() {
        pushStrategy.sendMetrics(mockPayload, null, mockManager)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_METRICS_PROCESSOR)
    }

    @Test
    fun `send metrics should send an uninstall metric if payload is an uninstall tracker`() {
        val notificationsEnabled = true
        `when`(mockPayload.isUninstallTracker).thenReturn(true)
        `when`(mockManager.areNotificationsEnabled()).thenReturn(notificationsEnabled)

        val mockInstanceId = "some-instance-id"
        given(mockPayload.instanceId).willReturn(mockInstanceId)

        pushStrategy.sendMetrics(mockPayload, mockMetricsProcessor, mockManager)

        verify(mockMetricsProcessor).sendUninstallTrackerRequest(mockInstanceId, !notificationsEnabled)
    }

    @Test
    fun `send metrics should send a received metric if the payload is not an uninstall tracker`() {
        val mockInstanceId = "some-instance-id"
        given(mockPayload.instanceId).willReturn(mockInstanceId)

        `when`(mockPayload.isUninstallTracker).thenReturn(false)

        pushStrategy.sendMetrics(mockPayload, mockMetricsProcessor, mockManager)

        verify(mockMetricsProcessor).sendReceivedRequest(mockInstanceId)
    }

}
