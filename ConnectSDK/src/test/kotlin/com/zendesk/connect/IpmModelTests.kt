package com.zendesk.connect

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito.verify

class IpmModelTests {

    private val mockIpmCoordinator = mock<IpmCoordinator>()

    private val ipmModel = IpmModel(mockIpmCoordinator)

    @Test
    fun `getIpm should call getIpm on the coordinator`() {
        ipmModel.ipm

        verify(mockIpmCoordinator).ipm
    }

    @Test
    fun `getAvatar should call getAvatar on the coordinator`() {
        ipmModel.avatar

        verify(mockIpmCoordinator).avatarImage
    }

    @Test
    fun `onAction should call handleIpmAction on the coordinator`() {
        ipmModel.onAction()

        verify(mockIpmCoordinator).handleIpmAction()
    }
}

@RunWith(Parameterized::class)
internal class IpmModelParameterizedTests(private val ipmDismissType: IpmDismissType) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "IpmDismissType.{0}")
        fun data() = IpmDismissType.values()
    }

    private val mockIpmCoordinator = mock<IpmCoordinator>()

    private val ipmModel = IpmModel(mockIpmCoordinator)

    @Test
    fun `onDismiss should call handleIpmDismiss on the coordinator`() {
        ipmModel.onDismiss(ipmDismissType)

        verify(mockIpmCoordinator).handleIpmDismiss(ipmDismissType)
    }
}
