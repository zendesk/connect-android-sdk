package com.zendesk.connect

import android.content.Context
import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import com.zendesk.logger.Logger
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException
import java.io.InputStream

@RunWith(MockitoJUnitRunner.Silent::class)
class IpmCoordinatorTests {

    companion object {
        private const val LOG_APP_IN_BACKGROUND = "App in the background, starting listeners and workers"
        private const val LOG_UNABLE_TO_RETRIEVE_IMAGE = "Unable to retrieve IPM avatar image, there will be none"
        private const val LOG_IPM_CROWD_OUT = "IPM with oid some-instance-id has been crowded out"
        private const val LOG_IPM_PURGED = "IPM with oid some-instance-id has been purged"
        private const val LOG_ON_ACTION_CALLED = "handleIpmAction() called"
        private const val LOG_ON_FOREGROUND = "onForeground() called"
        private const val LOG_ON_IPM_TIME_TO_LIVE_ENDED = "onIpmTimeToLiveEnded() called"
        private const val LOG_AVATAR_INPUT_STREAM_FAILED_TO_CLOSE = "Avatar image InputStream failed to close"
    }

    private val testUrl = "https://someurl.net"
    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    private val mockRepository = mock<IpmRepository>()
    private val mockContext = mock<Context>()
    private val mockNavigator = mock<Navigator>()
    private val mockClient = mock<OkHttpClient>()
    private val mockForegroundListener = mock<ForegroundListener>()
    private val mockConnectScheduler = mock<ConnectScheduler>()
    private val mockNotificationProcessor = mock<NotificationProcessor>()
    private val mockIpmMetricProcessor = mock<IpmMetricProcessor>()

    private val mockIpm = mock<IpmPayload>()
    private val mockInputStream = mock<InputStream>()
    private val mockCall = mock<Call>()
    private val mockResponse = mock<Response>()
    private val mockBody = mock<ResponseBody>()

    private val mockId = "some-instance-id"
    private val mockTimeToLive = 42L

    private val coordinator = spy(IpmCoordinator(
        mockRepository,
        mockContext,
        mockNavigator,
        mockClient,
        mockForegroundListener,
        mockConnectScheduler,
        mockNotificationProcessor,
        mockIpmMetricProcessor
    ))

    @Before
    fun setUp() {
        given(mockClient.newCall(any<Request>())).willReturn(mockCall)
        given(mockCall.execute()).willReturn(mockResponse)
        given(mockResponse.isSuccessful).willReturn(true)
        given(mockResponse.body()).willReturn(mockBody)
        given(mockBody.byteStream()).willReturn(mockInputStream)
        given(mockForegroundListener.isHostAppInTheForeground()).willReturn(true)
        given(mockForegroundListener.isActivityLastResumed(any())).willReturn(false)
        given(mockIpm.instanceId).willReturn(mockId)
        given(mockIpm.timeToLive).willReturn(mockTimeToLive)
    }

    // region startIpm
    @Test
    fun `startIpm should not fetch the avatar image for a null url`() {
        given(mockIpm.logo).willReturn(null)

        coordinator.startIpm(mockIpm)

        verifyZeroInteractions(mockClient)
    }

    @Test
    fun `startIpm should fetch the avatar image if a non-empty URL was provided`() {
        given(mockIpm.logo).willReturn(testUrl)

        coordinator.startIpm(mockIpm)

        verify(mockClient).newCall(any<Request>())
    }

    @Test
    fun `startIpm should log a message for the purged ipm if there is one currently in the repository`() {
        given(mockForegroundListener.isActivityLastResumed(IpmActivity::class.java)).willReturn(true)

        coordinator.startIpm(mockIpm)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_IPM_PURGED)
    }

    @Test
    fun `startIpm should not save the received ipm if there is one currently in the repository`() {
        given(mockForegroundListener.isActivityLastResumed(IpmActivity::class.java)).willReturn(true)

        coordinator.startIpm(mockIpm)

        verify(coordinator, never()).saveIpm(any(), any())
    }

    @Test
    fun `startIpm should not start the ipm if there is one currently in the repository`() {
        given(mockForegroundListener.isActivityLastResumed(IpmActivity::class.java)).willReturn(true)

        coordinator.startIpm(mockIpm)

        verify(mockNavigator, never()).startIpmActivity(any())
    }

    @Test
    fun `startIpm will not log a message for purged if there is no ipm currently in the repository`() {
        given(mockForegroundListener.isActivityLastResumed(IpmActivity::class.java)).willReturn(false)

        coordinator.startIpm(mockIpm)

        assertThat(logAppender.contains(LOG_IPM_PURGED)).isFalse()
    }

    @Test
    fun `startIpm should save the received ipm if the app is in the foreground`() {
        given(mockIpm.logo).willReturn(testUrl)
        willDoNothing().given(coordinator).saveIpm(mockIpm, mockInputStream)

        coordinator.startIpm(mockIpm)

        verify(coordinator).saveIpm(mockIpm, mockInputStream)
    }

    @Test
    fun `startIpm should save warm up the repository if the app is in the foreground`() {
        given(mockIpm.logo).willReturn(testUrl)
        willDoNothing().given(coordinator).saveIpm(mockIpm, mockInputStream)

        coordinator.startIpm(mockIpm)

        verify(mockRepository).warmUp()
    }

    @Test
    fun `startIpm should track displayed if the app is in the foreground`() {
        coordinator.startIpm(mockIpm)

        verify(mockIpmMetricProcessor).trackDisplayed(mockId)
    }

    @Test
    fun `startIpm should trigger the navigator to start the ipm activity`() {
        coordinator.startIpm(mockIpm)

        verify(mockNavigator).startIpmActivity(mockContext)
    }

    @Test
    fun `startIpm should log a message if the app is in the background`() {
        given(mockForegroundListener.isHostAppInTheForeground()).willReturn(false)

        coordinator.startIpm(mockIpm)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_APP_IN_BACKGROUND)
    }

    @Test
    fun `startIpm should log a message for crowd out if there was a previous ipm waiting and the app is in the background`() {
        given(mockForegroundListener.isHostAppInTheForeground()).willReturn(false)

        val mockPreviousIpm = mock<IpmPayload>()
        val mockPreviousIpmId = "some-id"
        given(mockRepository.getIpmPayload()).willReturn(mockPreviousIpm)
        given(mockPreviousIpm.instanceId).willReturn(mockPreviousIpmId)

        coordinator.startIpm(mockIpm)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_IPM_CROWD_OUT)
    }

    @Test
    fun `startIpm should not log a message for crowd out if there was not a previous ipm waiting and the app is in the background`() {
        given(mockForegroundListener.isHostAppInTheForeground()).willReturn(false)

        given(mockRepository.getIpmPayload()).willReturn(null)

        coordinator.startIpm(mockIpm)

        assertThat(logAppender.contains(LOG_IPM_CROWD_OUT)).isFalse()
    }

    @Test
    fun `startIpm should cancel the previous ipm time to live if the app is in the background`() {
        given(mockForegroundListener.isHostAppInTheForeground()).willReturn(false)

        val mockPreviousIpm = mock<IpmPayload>()
        val mockPreviousIpmId = "some-id"
        given(mockRepository.getIpmPayload()).willReturn(mockPreviousIpm)
        given(mockPreviousIpm.instanceId).willReturn(mockPreviousIpmId)

        coordinator.startIpm(mockIpm)

        verify(mockConnectScheduler).cancelIpmTimeToLive(mockPreviousIpmId)
    }

    @Test
    fun `startIpm should not cancel the previous ipm time to live if  there was not a previous ipm waiting the app is in the background`() {
        given(mockForegroundListener.isHostAppInTheForeground()).willReturn(false)

        given(mockRepository.getIpmPayload()).willReturn(null)

        coordinator.startIpm(mockIpm)

        verify(mockConnectScheduler, never()).cancelIpmTimeToLive(anyString())
    }

    @Test
    fun `startIpm should save the ipm if the app is in the background`() {
        given(mockForegroundListener.isHostAppInTheForeground()).willReturn(false)
        given(mockIpm.logo).willReturn(testUrl)
        willDoNothing().given(coordinator).saveIpm(mockIpm, mockInputStream)

        coordinator.startIpm(mockIpm)

        verify(coordinator).saveIpm(mockIpm, mockInputStream)
    }

    @Test
    fun `startIpm should register a foreground listener if the app is in the background`() {
        given(mockForegroundListener.isHostAppInTheForeground()).willReturn(false)

        coordinator.startIpm(mockIpm)

        verify(mockForegroundListener).addCallback(coordinator.foregroundCallback)
    }

    @Test
    fun `startIpm should schedule a time to live worker if the app is in the background`() {
        given(mockForegroundListener.isHostAppInTheForeground()).willReturn(false)

        coordinator.startIpm(mockIpm)

        verify(mockConnectScheduler).scheduleIpmTimeToLive(mockId, mockTimeToLive)
    }

    @Test
    fun `startIpm should not display the IPM if the app is in the background`() {
        given(mockForegroundListener.isHostAppInTheForeground()).willReturn(false)

        coordinator.startIpm(mockIpm)

        verifyZeroInteractions(mockNavigator)
    }

    @Test
    fun `startIpm should close the avatar image input stream if it was not null`() {
        given(mockIpm.logo).willReturn(testUrl)

        coordinator.startIpm(mockIpm)

        verify(mockInputStream).close()
    }

    @Test
    fun `startIpm should log a message if closing the avatar input stream threw an exception`() {
        given(mockIpm.logo).willReturn(testUrl)
        given(mockInputStream.close()).will { throw IOException() }

        coordinator.startIpm(mockIpm)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_AVATAR_INPUT_STREAM_FAILED_TO_CLOSE)
    }
    // endregion

    @Test
    fun `saveIpm should save the ipm data in the repository`() {
        coordinator.saveIpm(mockIpm, mockInputStream)

        verify(mockRepository).setIpmPayload(mockIpm)
    }

    @Test
    fun `saveIpm should save the avatar in the repository`() {
        coordinator.saveIpm(mockIpm, mockInputStream)

        verify(mockRepository).setAvatarImage(mockInputStream)
    }

    @Test
    fun `handleIpmAction should log a message`() {
        coordinator.handleIpmAction()

        assertThat(logAppender.lastLog()).isEqualTo(LOG_ON_ACTION_CALLED)
    }

    @Test
    fun `handleIpmAction should track action`() {
        coordinator.handleIpmAction()

        verify(mockIpmMetricProcessor).trackAction()
    }

    @Test
    fun `handleIpmAction should clear the IPM repository`() {
        coordinator.handleIpmAction()

        verify(mockRepository).clear()
    }

    @Test
    fun `onIpmTimeToLiveEnded should log a message`() {
        coordinator.onIpmTimeToLiveEnded()

        assertThat(logAppender.lastLog()).isEqualTo(LOG_ON_IPM_TIME_TO_LIVE_ENDED)
    }

    @Test
    fun `onIpmTimeToLiveEnded should remove the foreground listener callback`() {
        coordinator.onIpmTimeToLiveEnded()

        verify(mockForegroundListener).removeCallback(coordinator.foregroundCallback)
    }

    @Test
    fun `onIpmTimeToLiveEnded should not show a push notification if the stored ipm is null`() {
        given(mockRepository.getIpmPayload()).willReturn(null)

        coordinator.onIpmTimeToLiveEnded()

        verify(coordinator, never()).showAsPushNotification(any())
    }

    @Test
    fun `onIpmTimeToLiveEnded should show a push notification if fallback to push is true`() {
        given(mockRepository.getIpmPayload()).willReturn(mockIpm)
        willDoNothing().given(coordinator).showAsPushNotification(mockIpm)

        coordinator.onIpmTimeToLiveEnded()

        verify(coordinator).showAsPushNotification(mockIpm)
    }

    @Test
    fun `onIpmTimeToLiveEnded should clear the repository`() {
        coordinator.onIpmTimeToLiveEnded()

        verify(mockRepository).clear()
    }

    @Test
    fun `showAsPushNotification will call process with the expected payload`() {
        val mockHeading = "Some heading"
        val mockMessage = "Some message"
        val mockDeepLink = "some://deep/link"

        val expectedPayload = mapOf(
            ConnectNotification.Keys.INSTANCE_ID.key to mockId,
            ConnectNotification.Keys.NOTIFICATION_ID.key to "${mockId.hashCode()}",
            ConnectNotification.Keys.TITLE.key to mockHeading,
            ConnectNotification.Keys.BODY.key to mockMessage,
            ConnectNotification.Keys.DEEP_LINK.key to mockDeepLink
        )

        given(mockIpm.instanceId).willReturn(mockId)
        given(mockIpm.heading).willReturn(mockHeading)
        given(mockIpm.message).willReturn(mockMessage)
        given(mockIpm.action).willReturn(mockDeepLink)

        coordinator.showAsPushNotification(mockIpm)

        verify(mockNotificationProcessor).process(expectedPayload)
    }

    @Test
    fun `fetchAvatarImage should log a warning if there was an IOException`() {
        given(mockCall.execute()).will { throw IOException() }

        coordinator.fetchAvatarImage(testUrl)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_UNABLE_TO_RETRIEVE_IMAGE)
    }

    @Test
    fun `fetchAvatarImage should return null if there was an IOException`() {
        given(mockCall.execute()).will { throw IOException() }

        val result = coordinator.fetchAvatarImage(testUrl)

        assertThat(result).isNull()
    }

    @Test
    fun `fetchAvatarImage should return null if the response was not successful`() {
        given(mockResponse.isSuccessful).willReturn(false)

        val result = coordinator.fetchAvatarImage(testUrl)

        assertThat(result).isNull()
    }

    @Test
    fun `fetchAvatarImage should log a message if the response was not successful`() {
        given(mockResponse.isSuccessful).willReturn(false)

        coordinator.fetchAvatarImage(testUrl)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_UNABLE_TO_RETRIEVE_IMAGE)
    }

    @Test
    fun `fetchAvatarImage should return null if the response body was null`() {
        given(mockResponse.body()).willReturn(null)

        val result = coordinator.fetchAvatarImage(testUrl)

        assertThat(result).isNull()
    }

    @Test
    fun `fetchAvatarImage should return the input stream retrieved in the network request`() {
        val result = coordinator.fetchAvatarImage(testUrl)

        assertThat(result).isEqualTo(mockInputStream)
    }

    @Test
    fun `getIpmPayload should retrieve the ipm model stored in the repository`() {
        given(mockRepository.getIpmPayload()).willReturn(mockIpm)

        assertThat(coordinator.ipm).isEqualTo(mockIpm)
    }

    @Test
    fun `getAvatarImage should retrieve the avatar image stored in the repository`() {
        val mockBitmap = mock<Bitmap>()
        given(mockRepository.getAvatarImage()).willReturn(mockBitmap)

        assertThat(coordinator.avatarImage).isEqualTo(mockBitmap)
    }

    @Test
    fun `getForegroundCallback should always return the same instance`() {
        assertThat(coordinator.getForegroundCallback()).isEqualTo(coordinator.getForegroundCallback())
    }

    @Test
    fun `onForeground should log a message`() {
        coordinator.getForegroundCallback().onForeground()

        assertThat(logAppender.lastLog()).isEqualTo(LOG_ON_FOREGROUND)
    }

    @Test
    fun `onForeground should warm up the repository`() {
        coordinator.getForegroundCallback().onForeground()

        verify(mockRepository).warmUp()
    }

    @Test
    fun `onForeground should remove the foreground listener`() {
        coordinator.getForegroundCallback().onForeground()

        verify(mockForegroundListener).removeCallback(any<IpmCoordinator.ForegroundCallback>())
    }

    @Test
    fun `onForeground should set its reference to null`() {
        coordinator.getForegroundCallback().onForeground()

        assertThat(coordinator.foregroundCallback).isNull()
    }

    @Test
    fun `onForeground should cancel the worker if the repository doesn't return null`() {
        given(mockRepository.getIpmPayload()).willReturn(mockIpm)

        coordinator.getForegroundCallback().onForeground()

        verify(mockConnectScheduler).cancelIpmTimeToLive(mockId)
    }

    @Test
    fun `onForeground should not cancel the worker if the repository return null`() {
        given(mockRepository.getIpmPayload()).willReturn(null)

        coordinator.getForegroundCallback().onForeground()

        verify(mockConnectScheduler, never()).cancelIpmTimeToLive(any())
    }

    @Test
    fun `onForeground should track displayed if the repository doesn't return null`() {
        given(mockRepository.getIpmPayload()).willReturn(mockIpm)

        coordinator.getForegroundCallback().onForeground()

        verify(mockIpmMetricProcessor).trackDisplayed(mockId)
    }

    @Test
    fun `onForeground should not track displayed if the repository return null`() {
        given(mockRepository.getIpmPayload()).willReturn(null)

        coordinator.getForegroundCallback().onForeground()

        verify(mockIpmMetricProcessor, never()).trackDisplayed(mockId)
    }

    @Test
    fun `onForeground should start the ipm if the repository doesn't return null`() {
        given(mockRepository.getIpmPayload()).willReturn(mockIpm)

        coordinator.getForegroundCallback().onForeground()

        verify(mockNavigator).startIpmActivity(mockContext)
    }

    @Test
    fun `onForeground should not start the ipm if the repository return null`() {
        given(mockRepository.getIpmPayload()).willReturn(null)

        coordinator.getForegroundCallback().onForeground()

        verify(mockNavigator, never()).startIpmActivity(any())
    }
}

@RunWith(Parameterized::class)
internal class IpmCoordinatorParameterizedTests(private val ipmDismissType: IpmDismissType) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "IpmDismissType.{0}")
        fun data() = IpmDismissType.values()

        private const val LOG_FORMAT_DISMISS_MESSAGE = "handleIpmDismiss(IpmDismissType) called, IpmDismissType is: %s"
    }

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    private val mockRepository = mock<IpmRepository>()
    private val mockContext = mock<Context>()
    private val mockNavigator = mock<Navigator>()
    private val mockClient = mock<OkHttpClient>()
    private val mockForegroundListener = mock<ForegroundListener>()
    private val mockConnectScheduler = mock<ConnectScheduler>()
    private val mockNotificationProcessor = mock<NotificationProcessor>()
    private val mockIpmMetricProcessor = mock<IpmMetricProcessor>()

    private val coordinator = IpmCoordinator(
        mockRepository,
        mockContext,
        mockNavigator,
        mockClient,
        mockForegroundListener,
        mockConnectScheduler,
        mockNotificationProcessor,
        mockIpmMetricProcessor
    )

    @Test
    fun `handleIpmDismiss should log a message with the dismiss type`() {
        coordinator.handleIpmDismiss(ipmDismissType)

        assertThat(logAppender.lastLog()).isEqualTo(String.format(LOG_FORMAT_DISMISS_MESSAGE, ipmDismissType))
    }

    @Test
    fun `handleIpmDismiss should track dismiss`() {
        coordinator.handleIpmDismiss(ipmDismissType)

        verify(mockIpmMetricProcessor).trackDismiss()
    }

    @Test
    fun `handleIpmDismiss should clear the ipm repository`() {
        coordinator.handleIpmDismiss(ipmDismissType)

        verify(mockRepository).clear()
    }
}
