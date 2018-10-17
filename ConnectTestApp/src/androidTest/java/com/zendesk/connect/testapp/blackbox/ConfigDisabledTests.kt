package com.zendesk.connect.testapp.blackbox

import com.google.common.truth.Truth.assertThat
import com.zendesk.connect.resetConnect
import com.zendesk.connect.testInitConnect
import com.zendesk.connect.testapp.helpers.clearFiles
import com.zendesk.connect.testapp.helpers.clearSharedPrefs
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RequestsVerifier.verifyRequest
import io.appflate.restmock.utils.RequestMatchers
import io.appflate.restmock.utils.RequestMatchers.pathEndsWith
import io.outbound.sdk.Outbound
import io.outbound.sdk.testInitOutbound
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Black box tests for SDK behaviour when the SDK if initialised and config is disabled,
 * which should result in the SDK not making any network requests.
 */
class ConfigDisabledTests {

    @Before
    fun setUp() {
        resetConnect()
        RESTMockServer.reset()
        clearSharedPrefs()
        clearFiles()

        RESTMockServer.whenGET(RequestMatchers.pathContains(configPath))
                .thenReturnFile(200, "config_disabled_response.json")

        testInitConnect(testClient) // This is done internally by Outbound
        testInitOutbound(testApplication, "Whatever", "Whatevs", testClient)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun callingIdentifyUserWithADisabledConfigShouldMakeNoRequestToTheApi() {
        Outbound.identify(testUser)

        verifyRequest(pathEndsWith(identifyPath)).never()
    }

    @Test
    fun callingTrackEventWithADisabledConfigShouldMakeNoRequestToTheApi() {
        Outbound.track(testEvent)

        verifyRequest(pathEndsWith(trackPath)).never()
    }

    @Test
    fun callingRegisterForPushWithADisabledConfigShouldMakeNoRequestToTheApi() {
        Outbound.register()

        verifyRequest(pathEndsWith(registerPath)).never()
    }

    @Test
    fun callingDisablePushNotificationsWithADisabledConfigShouldMakeNoRequestToTheApi() {
        Outbound.disable()

        verifyRequest(pathEndsWith(disablePath)).never()
    }

    @Test
    fun pairDeviceShouldReturnFalseAndMakeNoRequestToTheApiIfConfigIsDisabled() {
        assertThat(Outbound.pairDevice("0000")).isFalse()

        verifyRequest(pathEndsWith(pairPath)).never()
    }

}