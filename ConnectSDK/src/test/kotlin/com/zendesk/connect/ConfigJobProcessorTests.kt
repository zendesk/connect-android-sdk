package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.zendesk.logger.Logger
import com.zendesk.test.MockedSharedPreferences
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

@RunWith(MockitoJUnitRunner.Silent::class)
class ConfigJobProcessorTests {

    private val NULL_CONTROLLER_WARNING = "Config provider and storage controller must not be null"
    private val IO_EXCEPTION_WARNING = "Error while sending config request"

    private val gson = Gson()

    private val testConfig = Config(true, null)

    private lateinit var storageControllerSpy: StorageController

    @Mock
    private lateinit var mockConfigProvider: ConfigProvider

    @Mock
    private lateinit var mockConfigCall: Call<Config>

    @Mock
    private lateinit var mockConfigResponse: Response<Config>

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(ConfigJobProcessorTests::class.java)

        val preferencesStorage = SharedPreferencesStorage(MockedSharedPreferences().getSharedPreferences(), gson)

        storageControllerSpy = spy(StorageController(preferencesStorage))

        `when`(mockConfigProvider.config(any<String>(), any<String>())).thenReturn(mockConfigCall)

        `when`(mockConfigCall.execute()).thenReturn(mockConfigResponse)
    }

    @After
    fun tearDown() {

    }

    @Test
    fun `null config provider should log a warning and make no config request`() {
        ConfigJobProcessor.process(null, storageControllerSpy)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_CONTROLLER_WARNING)

        verifyZeroInteractions(mockConfigProvider)
    }

    @Test
    fun `null storage should log a warning and make no config request`() {
        ConfigJobProcessor.process(mockConfigProvider, null)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_CONTROLLER_WARNING)

        verifyZeroInteractions(mockConfigProvider)
    }

    @Test
    fun `returned config should be stored into the provided storage mechanism`() {
        `when`(mockConfigResponse.isSuccessful).thenReturn(true)
        `when`(mockConfigResponse.body()).thenReturn(testConfig)

        ConfigJobProcessor.process(mockConfigProvider, storageControllerSpy)

        verify(storageControllerSpy).saveConfig(testConfig)
    }

    @Test
    fun `io exceptions thrown by config requests should be caught and log a warning`() {
        `when`(mockConfigCall.execute()).thenThrow(IOException())

        ConfigJobProcessor.process(mockConfigProvider, storageControllerSpy)

        verifyZeroInteractions(storageControllerSpy)

        assertThat(logAppender.lastLog()).contains(IO_EXCEPTION_WARNING)
    }

    @Test
    fun `a non successful config request not affect storage`() {
        `when`(mockConfigResponse.code()).thenReturn(400)

        ConfigJobProcessor.process(mockConfigProvider, storageControllerSpy)

        verifyZeroInteractions(storageControllerSpy)
    }
}