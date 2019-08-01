package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import com.zendesk.connect.ConnectActionService.ACTION_OPEN_NOTIFICATION
import com.zendesk.connect.ConnectActionService.EXTRA_NOTIFICATION
import org.junit.Test

internal class ConnectActionServiceTest {

    @Test
    fun `ACTION_OPEN_NOTIFICATION value should be correct`() {
        assertThat(ACTION_OPEN_NOTIFICATION).isEqualTo(".connect.action.OPEN_NOTIFICATION")
    }

    @Test
    fun `EXTRA_NOTIFICATION value should be correct`() {
        assertThat(EXTRA_NOTIFICATION).isEqualTo("com.zendesk.connect.extra.NOTIFICATION")
    }

    @Test
    fun `openLaunchActivity default value should be true`() {
        assertThat(ConnectActionService.openLaunchActivity).isTrue()
    }

    @Test
    fun `handleDeepLinks default value should be true`() {
        assertThat(ConnectActionService.handleDeepLinks).isTrue()
    }

    @Test
    fun `shouldOpenLaunchActivityByDefault should updated the value of openLaunchActivity`() {
        ConnectActionService.shouldOpenLaunchActivityByDefault(false)

        assertThat(ConnectActionService.openLaunchActivity).isFalse()
    }

    @Test
    fun `shouldHandleDeepLinks should updated the value of handleDeepLinks`() {
        ConnectActionService.shouldHandleDeepLinks(false)

        assertThat(ConnectActionService.handleDeepLinks).isFalse()
    }
}
