package com.zendesk.connect

import android.content.Context
import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import com.zendesk.connect.IpmRepository.AVATAR_FILE_NAME
import com.zendesk.connect.IpmRepository.IPM_FILE_NAME
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willReturn
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import java.io.InputStream

class IpmRepositoryTests {

    private val mockIpm = mock<IpmPayload>()
    private val mockInputStream = mock<InputStream>()
    private val mockBitmap = mock<Bitmap>()

    private val mockFileStorage = mock<FileStorage>()
    private val mockContext = mock<Context>()
    private val mockBitmapTransformer = mock<BitmapTransformer>()

    private val repository = spy(IpmRepository(mockFileStorage, mockContext, mockBitmapTransformer))

    @Test
    fun `ipmPayload initial value should be null`() {
        assertThat(repository.ipmPayload).isNull()
    }

    @Test
    fun `avatarImage initial value should be null`() {
        assertThat(repository.avatarImage).isNull()
    }

    @Test
    fun `setIpmPayload should store the given IPM in the file storage`() {
        repository.setIpmPayload(mockIpm)

        verify(mockFileStorage).saveToFile(mockIpm, IPM_FILE_NAME)
    }

    @Test
    fun `setIpmPayload should delete any file from the storage if null was received`() {
        repository.setIpmPayload(null)

        verify(mockFileStorage).deleteFile(IPM_FILE_NAME)
    }

    @Test
    fun `setIpmPayload should set ipmPayload to null`() {
        repository.setIpmPayload(mockIpm)

        assertThat(repository.ipmPayload).isNull()
    }

    @Test
    fun `getIpmPayload should retrieve the file from the storage if ipmPayload is null`() {
        repository.getIpmPayload()

        verify(mockFileStorage).getFileAsObject(IPM_FILE_NAME)
    }

    @Test
    fun `getIpmPayload should not call the file storage if ipmPayload was not null`() {
        repository.ipmPayload = mockIpm

        repository.getIpmPayload()

        verify(mockFileStorage, never()).getFileAsObject(IPM_FILE_NAME)
    }

    @Test
    fun `getIpmPayload should return null if nothing is stored`() {
        given(mockFileStorage.getFileAsObject(IPM_FILE_NAME)).willReturn(null)

        assertThat(repository.ipmPayload).isNull()
    }

    @Test
    fun `setAvatarImage should store the given image in the file storage`() {
        repository.setAvatarImage(mockInputStream)

        verify(mockFileStorage).saveToFile(mockInputStream, AVATAR_FILE_NAME)
    }

    @Test
    fun `setAvatarImage should delete any file from the storage if null was received`() {
        repository.setAvatarImage(null)

        verify(mockFileStorage).deleteFile(AVATAR_FILE_NAME)
    }

    @Test
    fun `setAvatarImage should set avatarImage to null`() {
        repository.setAvatarImage(mockInputStream)

        assertThat(repository.avatarImage).isNull()
    }

    @Test
    fun `getAvatarImage should retrieve the file from the storage if avatarImage is null`() {
        repository.getAvatarImage()

        verify(mockFileStorage).getFileAsInputStream(AVATAR_FILE_NAME)
    }

    @Test
    fun `getAvatarImage should transform the bitmap if retrieved from storage`() {
        given(mockFileStorage.getFileAsInputStream(AVATAR_FILE_NAME))
            .willReturn(mockInputStream)

        repository.getAvatarImage()

        verify(mockBitmapTransformer).toRoundedBitmap(mockInputStream, mockContext)
    }

    @Test
    fun `getAvatarImage should return the transformed bitmap if retrieved from storage`() {
        given(mockFileStorage.getFileAsInputStream(AVATAR_FILE_NAME))
            .willReturn(mockInputStream)
        given(mockBitmapTransformer.toRoundedBitmap(mockInputStream, mockContext))
            .willReturn(mockBitmap)

        assertThat(repository.getAvatarImage()).isEqualTo(mockBitmap)
    }

    @Test
    fun `getAvatarImage should not retrieve the file from the storage if avatarImage is not null`() {
        repository.avatarImage = mockBitmap

        repository.getAvatarImage()

        verify(mockFileStorage, never()).getFileAsInputStream(AVATAR_FILE_NAME)
    }

    @Test
    fun `getAvatarImage should return null if nothing is stored`() {
        given(mockFileStorage.getFileAsInputStream(AVATAR_FILE_NAME)).willReturn(null)

        assertThat(repository.avatarImage).isNull()
    }

    @Test
    fun `getAvatarImage should not transform the bitmap if nothing is stored`() {
        given(mockFileStorage.getFileAsInputStream(AVATAR_FILE_NAME)).willReturn(null)

        verify(mockBitmapTransformer, never()).toRoundedBitmap(any(), any())
    }

    @Test
    fun `warmUp should call getIpmPayload`() {
        willReturn(mockIpm).given(repository).getIpmPayload()

        repository.warmUp()

        verify(repository).getIpmPayload()
    }

    @Test
    fun `warmUp should call getAvatarImage`() {
        willReturn(mockBitmap).given(repository).getAvatarImage()

        repository.warmUp()

        verify(repository).getAvatarImage()
    }

    @Test
    fun `clear should delete the ipm from the file storage`() {
        repository.clear()

        verify(mockFileStorage).deleteFile(IPM_FILE_NAME)
    }

    @Test
    fun `clear should delete the avatar from the file storage`() {
        repository.clear()

        verify(mockFileStorage).deleteFile(AVATAR_FILE_NAME)
    }

    @Test
    fun `clear should set ipmPayload to null`() {
        repository.ipmPayload = mockIpm

        repository.clear()

        assertThat(repository.ipmPayload).isNull()
    }

    @Test
    fun `clear should set avatarImage to null`() {
        repository.avatarImage = mockBitmap

        repository.clear()

        assertThat(repository.avatarImage).isNull()
    }
}
