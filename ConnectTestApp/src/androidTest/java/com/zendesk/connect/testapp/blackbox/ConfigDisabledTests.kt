package com.zendesk.connect.testapp.blackbox

import android.support.test.espresso.IdlingRegistry
import com.google.common.truth.Truth.assertThat
import com.zendesk.connect.testapp.helpers.clearDatabase
import com.zendesk.connect.testapp.helpers.clearSharedPrefs
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RequestsVerifier.verifyRequest
import io.appflate.restmock.utils.RequestMatchers
import io.appflate.restmock.utils.RequestMatchers.pathEndsWith
import io.outbound.sdk.initSdkForTesting
import io.outbound.sdk.Outbound
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Black box tests for SDK behaviour when the SDK if initialised and config is disabled,
 * which should result in the SDK not making any network requests.
 */
class ConfigDisabledTests {

    private lateinit var latch: CountDownLatch

    @Before
    fun setUp() {
        RESTMockServer.reset()
        clearSharedPrefs()
        clearDatabase()

        latch = CountDownLatch(1) // init

        IdlingRegistry.getInstance().register(idlingClient)

        idlingClient.registerIdleTransitionCallback { latch.countDown() }

        RESTMockServer.whenGET(RequestMatchers.pathContains(configPath))
                .thenReturnFile(200, "config_disabled_response.json")

        initSdkForTesting(testApplication, "Whatever",
        "Whatevs", testClient)

        // Need to wait for the config request to return the disabled config
        latch.await(2, TimeUnit.SECONDS)
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
    fun pairDeviceShouldReturnFalseIfConfigIsDisabled() {
        assertThat(Outbound.pairDevice("0000")).isFalse()
    }

}