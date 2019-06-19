package com.zendesk.connect.testapp.blackbox

import android.support.test.InstrumentationRegistry
import com.zendesk.connect.Connect
import com.zendesk.connect.resetConnect
import com.zendesk.connect.storeDisabledConfig
import com.zendesk.connect.testInitConnect
import com.zendesk.connect.testapp.helpers.clearSharedPrefs
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RESTMockServerStarter
import io.appflate.restmock.RequestsVerifier.verifyRequest
import io.appflate.restmock.android.AndroidAssetsFileParser
import io.appflate.restmock.android.AndroidLogger
import io.appflate.restmock.utils.RequestMatchers
import io.appflate.restmock.utils.RequestMatchers.pathEndsWith
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Black box tests for SDK behaviour when the SDK if initialised and config is disabled,
 * which should result in the SDK not making any network requests.
 */
class ConfigDisabledTests {

    init {
        RESTMockServerStarter.startSync(
                AndroidAssetsFileParser(InstrumentationRegistry.getTargetContext()),
                AndroidLogger()
        )

        RESTMockServer.reset()

        RESTMockServer.whenGET(RequestMatchers.pathContains(configPath))
                .thenReturnFile(200, "config_disabled_response.json")
    }

    @Before
    fun setUp() {
        testInitConnect(testClient, shouldMakeConfigCall = false)
        storeDisabledConfig()
    }

    @After
    fun tearDown() {
        clearSharedPrefs()
        resetConnect()
    }

    @Test
    fun callingIdentifyUserWithADisabledConfigShouldMakeNoRequestToTheApi() {
        Connect.INSTANCE.identifyUser(testUser)

        verifyRequest(pathEndsWith(identifyPath)).never()
    }

    @Test
    fun callingTrackEventWithADisabledConfigShouldMakeNoRequestToTheApi() {
        Connect.INSTANCE.trackEvent(testEvent)

        verifyRequest(pathEndsWith(trackPath)).never()
    }

    @Test
    fun callingRegisterForPushWithADisabledConfigShouldMakeNoRequestToTheApi() {
        Connect.INSTANCE.registerForPush()

        verifyRequest(pathEndsWith(registerPath)).never()
    }

    @Test
    fun callingDisablePushNotificationsWithADisabledConfigShouldMakeNoRequestToTheApi() {
        Connect.INSTANCE.disablePush()

        verifyRequest(pathEndsWith(disablePath)).never()
    }

}
