package com.zendesk.connect

import android.app.Application
import com.google.common.truth.Truth.assertThat
import com.zendesk.logger.Logger
import org.junit.Test
import org.mockito.BDDMockito.willDoNothing
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

class ForegroundListenerTest {

    companion object {
        private const val LOG_START_LISTENING_ADD_CALLBACK = "addCallback - Adding callback"
        private const val LOG_START_LISTENING_SAME_CALLBACK = "addCallback - Callback was already registered"
        private const val LOG_REMOVE_CALLBACK = "removeCallback - Removing callback"
    }

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    private val mockApplication = mock<Application>()
    private val mockCallback = mock<ForegroundListener.Callback>()

    private val foregroundListener = spy(ForegroundListener(mockApplication))

    @Test
    fun `isHostAppInTheForeground initial value should be false`() {
        assertThat(foregroundListener.isHostAppInTheForeground).isFalse()
    }
    
    @Test
    fun `lastResumedActivityClass initial value should be null`() {
        assertThat(foregroundListener.lastResumedActivityClass).isNull()
    }

    @Test
    fun `callbacks initial value should be empty`() {
        assertThat(foregroundListener.callbacks).isEmpty()
    }

    @Test
    fun `instantiating the ForegroundListener should register for activity lifecycle callbacks`() {
        val instance = ForegroundListener(mockApplication)

        verify(mockApplication).registerActivityLifecycleCallbacks(instance)
    }

    @Test
    fun `isActivityLastResumed should return false if lastResumedActivityClass is null`() {
        foregroundListener.lastResumedActivityClass = null

        assertThat(foregroundListener.isActivityLastResumed(IpmActivity::class.java)).isFalse()
    }

    @Test
    fun `isActivityLastResumed should return false if lastResumedActivityClass is different`() {
        foregroundListener.lastResumedActivityClass = AdminActivity::class.java

        assertThat(foregroundListener.isActivityLastResumed(IpmActivity::class.java)).isFalse()
    }

    @Test
    fun `isActivityLastResumed should return false if lastResumedActivityClass is the same`() {
        foregroundListener.lastResumedActivityClass = IpmActivity::class.java

        assertThat(foregroundListener.isActivityLastResumed(IpmActivity::class.java)).isTrue()
    }

    // region addCallback
    @Test
    fun `addCallback should add the callback to the list`() {
        foregroundListener.addCallback(mockCallback)

        assertThat(foregroundListener.callbacks).isEqualTo(listOf(mockCallback))
    }

    @Test
    fun `addCallback should log a message for the added callback`() {
        foregroundListener.addCallback(mockCallback)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_START_LISTENING_ADD_CALLBACK)
    }

    @Test
    fun `addCallback should not add the same listener more than once`() {
        foregroundListener.addCallback(mockCallback)
        foregroundListener.addCallback(mockCallback)

        assertThat(foregroundListener.callbacks).isEqualTo(listOf(mockCallback))
    }

    @Test
    fun `addCallback should log a message if the listener was already added`() {
        foregroundListener.addCallback(mockCallback)
        foregroundListener.addCallback(mockCallback)

        assertThat(logAppender.lastLog()).isEqualTo(LOG_START_LISTENING_SAME_CALLBACK)
    }

    @Test
    fun `addCallback should add the listener if it wasn't already added`() {
        val otherCallback = ForegroundListener.Callback { }

        foregroundListener.addCallback(mockCallback)
        foregroundListener.addCallback(otherCallback)

        assertThat(foregroundListener.callbacks).isEqualTo(listOf(mockCallback, otherCallback))
    }
    // endregion

    // region removeCallback
    @Test
    fun `removeCallback should remove the callback from the list`() {
        foregroundListener.addCallback(mockCallback)
        foregroundListener.removeCallback(mockCallback)

        assertThat(foregroundListener.callbacks).isEmpty()
    }

    @Test
    fun `removeCallback should log a message for the removed callback`() {
        foregroundListener.addCallback(mockCallback)
        foregroundListener.removeCallback(mockCallback)

        assertThat(logAppender.contains(LOG_REMOVE_CALLBACK)).isTrue()
    }
    // endregion

    // region onActivityResumed
    @Test
    fun `onActivityResumed should set isHostAppInTheForeground to true`() {
        willDoNothing().given(foregroundListener).setupHandler()

        foregroundListener.onActivityResumed(mock())

        assertThat(foregroundListener.isHostAppInTheForeground).isTrue()
    }

    @Test
    fun `onActivityResumed should set lastResumedActivityClass to the class of the received activity`() {
        willDoNothing().given(foregroundListener).setupHandler()

        foregroundListener.onActivityResumed(mock<IpmActivity>())

        assertThat(foregroundListener.lastResumedActivityClass).isEqualTo(IpmActivity::class.java)
    }

    @Test
    fun `onActivityResumed should call setupHandler if callbacks are not empty`() {
        foregroundListener.callbacks.add(mock())
        willDoNothing().given(foregroundListener).setupHandler()

        foregroundListener.onActivityResumed(mock())

        verify(foregroundListener).setupHandler()
    }

    @Test
    fun `onActivityResumed should not call setupHandler if callbacks are empty`() {
        willDoNothing().given(foregroundListener).setupHandler()

        foregroundListener.onActivityResumed(mock())

        verify(foregroundListener, never()).setupHandler()
    }
    // endregion

    @Test
    fun `onActivityPaused should call reset handler`() {
        willDoNothing().given(foregroundListener).resetHandler()

        foregroundListener.onActivityPaused(mock())

        verify(foregroundListener).resetHandler()
    }

    @Test
    fun `onActivityPaused should set isHostAppInTheForeground to false`() {
        foregroundListener.onActivityPaused(mock())

        assertThat(foregroundListener.isHostAppInTheForeground).isFalse()
    }

    @Test
    fun `onActivityPaused should set lastResumedActivityClass to null`() {
        foregroundListener.onActivityPaused(mock())

        assertThat(foregroundListener.lastResumedActivityClass).isNull()
    }

    @Test
    fun `onForeground should alert all the callbacks`() {
        willDoNothing().given(foregroundListener).resetHandler()
        foregroundListener.addCallback(mockCallback)

        foregroundListener.onForeground()

        verify(mockCallback).onForeground()
    }

    @Test
    fun `onForeground should call reset handler`() {
        willDoNothing().given(foregroundListener).resetHandler()

        foregroundListener.onForeground()

        verify(foregroundListener).resetHandler()
    }
}
