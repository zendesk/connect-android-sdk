package com.zendesk.connect

import android.support.test.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.zendesk.connect.testapp.blackbox.disablePath
import com.zendesk.connect.testapp.blackbox.identifyPath
import com.zendesk.connect.testapp.blackbox.registerPath
import com.zendesk.connect.testapp.blackbox.testClient
import com.zendesk.connect.testapp.blackbox.testEvent
import com.zendesk.connect.testapp.blackbox.testUser
import com.zendesk.connect.testapp.blackbox.trackPath
import com.zendesk.connect.testapp.helpers.TestLogAppender
import com.zendesk.logger.Logger
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RESTMockServerStarter
import io.appflate.restmock.RequestsVerifier.verifyRequest
import io.appflate.restmock.android.AndroidAssetsFileParser
import io.appflate.restmock.android.AndroidLogger
import io.appflate.restmock.utils.RequestMatchers.pathEndsWith
import org.junit.After
import org.junit.Before
import org.junit.Test

class ConnectTests {

    private val SDK_NOT_INITIALISED_WARNING = "Connect SDK is not enabled"

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    @Before
    fun setUp() {
        RESTMockServerStarter.startSync(AndroidAssetsFileParser(InstrumentationRegistry.getContext()), AndroidLogger())
    }

    @After
    fun tearDown() {
        RESTMockServer.reset()
        resetConnect()
    }

    @Test
    fun isInitialisedShouldReturnTrueIfSdkIsInitialised() {
        testInitConnect(testClient)

        assertThat(Connect.INSTANCE.isInitialised).isTrue()
    }

    @Test
    fun isInitialisedShouldReturnFalseIfSdkIsNotInitialised() {
        assertThat(Connect.INSTANCE.isInitialised).isFalse()
    }

    @Test
    fun callingIdentifyUserBeforeTheSdkIsInitialisedShouldLogAWarning() {
        Connect.INSTANCE.identifyUser(testUser)

        assertThat(logAppender.lastLog()).isEqualTo(SDK_NOT_INITIALISED_WARNING)
    }

    @Test
    fun callingIdentifyUserBeforeTheSdkIsInitialisedShouldMakeNoNetworkRequest() {
        Connect.INSTANCE.identifyUser(testUser)

        verifyRequest(pathEndsWith(identifyPath)).never()
    }

    @Test
    fun callingTrackEventBeforeTheSdkIsInitialisedShouldLogAWarning() {
        Connect.INSTANCE.trackEvent(testEvent)

        assertThat(logAppender.lastLog()).isEqualTo(SDK_NOT_INITIALISED_WARNING)
    }

    @Test
    fun callingTrackEventBeforeTheSdkIsInitialisedShouldMakeNoNetworkRequest() {
        Connect.INSTANCE.trackEvent(testEvent)

        verifyRequest(pathEndsWith(trackPath)).never()
    }

    @Test
    fun callingRegisterForPushBeforeTheSdkIsInitialisedShouldLogAWarning() {
        Connect.INSTANCE.registerForPush()

        assertThat(logAppender.lastLog()).isEqualTo(SDK_NOT_INITIALISED_WARNING)
    }

    @Test
    fun callingRegisterForPushBeforeTheSdkIsInitialisedShouldMakeNoNetworkRequest() {
        Connect.INSTANCE.registerForPush()

        verifyRequest(pathEndsWith(registerPath)).never()
    }

    @Test
    fun callingDisablePushBeforeTheSdkIsInitialisedShouldLogAWarning() {
        Connect.INSTANCE.disablePush()

        assertThat(logAppender.lastLog()).isEqualTo(SDK_NOT_INITIALISED_WARNING)
    }

    @Test
    fun callingDisablePushBeforeTheSdkIsInitialisedShouldMakeNoNetworkRequest() {
        Connect.INSTANCE.disablePush()

        verifyRequest(pathEndsWith(disablePath)).never()
    }

    @Test
    fun callingLogoutBeforeTheSdkIsInitialisedShouldLogAWarning() {
        Connect.INSTANCE.logoutUser()

        assertThat(logAppender.lastLog()).isEqualTo(SDK_NOT_INITIALISED_WARNING)
    }

    @Test
    fun callingLogoutBeforeTheSdkIsInitialisedShouldMakeNoNetworkRequest() {
        Connect.INSTANCE.logoutUser()

        // logoutUser should attempt to disable push if initialised
        verifyRequest(pathEndsWith(disablePath)).never()
    }
}