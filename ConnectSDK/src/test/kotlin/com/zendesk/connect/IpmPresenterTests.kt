package com.zendesk.connect

import android.content.Intent
import android.graphics.Bitmap
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify

class IpmPresenterTests {

    private val mockView = mock<IpmMvp.View>()
    private val mockModel = mock<IpmMvp.Model>()
    private val mockConnectActionProcessor = mock<ConnectActionProcessor>()

    private val mockIpm = mock<IpmPayload>()
    private val mockAvatar = mock<Bitmap>()

    private val presenter = IpmPresenter(mockView, mockModel, mockConnectActionProcessor)

    private val mockAction = "some://action"

    @Test
    fun `onIpmReceived should display the ipm if it is returned by the model`() {
        given(mockModel.ipm).willReturn(mockIpm)

        presenter.onIpmReceived()

        verify(mockView).displayIpm(mockIpm)
    }

    @Test
    fun `onIpmReceived should display the avatar if it is returned by the model`() {
        given(mockModel.avatar).willReturn(mockAvatar)

        presenter.onIpmReceived()

        verify(mockView).displayAvatar(mockAvatar)
    }

    @Test
    fun `onIpmReceived should dismiss the ipm if the model returns null`() {
        given(mockModel.ipm).willReturn(null)

        presenter.onIpmReceived()

        verify(mockView).dismissIpm()
    }

    @Test
    fun `onIpmReceived should hide the avatar if the model returns null`() {
        given(mockModel.avatar).willReturn(null)

        presenter.onIpmReceived()

        verify(mockView).hideAvatar()
    }

    @Test
    fun `onAction should call onAction on the model`() {
        presenter.onAction(mockAction)

        verify(mockModel).onAction()
    }

    @Test
    fun `onAction should launch the deep link action if the action was a valid deep link`() {
        val mockIntent = mock<Intent>()
        given(mockConnectActionProcessor.resolveDeepLinkIntent(mockAction))
            .willReturn(mockIntent)

        presenter.onAction(mockAction)

        verify(mockView).launchActionDeepLink(mockIntent)
    }
    
    @Test
    fun `onAction should dismiss the ipm if the action was not a deep link`() {
        given(mockConnectActionProcessor.resolveDeepLinkIntent(mockAction))
            .willReturn(null)

        presenter.onAction(mockAction)

        verify(mockView).dismissIpm()
    }
}

@RunWith(Parameterized::class)
internal class IpmPresenterParameterizedTests(private val ipmDismissType: IpmDismissType) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "IpmDismissType.{0}")
        fun data() = IpmDismissType.values()
    }

    private val mockView = mock<IpmMvp.View>()
    private val mockModel = mock<IpmMvp.Model>()
    private val mockConnectActionProcessor = mock<ConnectActionProcessor>()

    private val presenter = IpmPresenter(mockView, mockModel, mockConnectActionProcessor)

    @Test
    fun `onDismiss should call onDismiss on the model`() {
        presenter.onDismiss(ipmDismissType)

        verify(mockModel).onDismiss(ipmDismissType)
    }

    @Test
    fun `onDismiss should dismiss the ipm`() {
        presenter.onDismiss(ipmDismissType)

        verify(mockView).dismissIpm()
    }
}
