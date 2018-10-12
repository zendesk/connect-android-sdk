package com.zendesk.connect.testapp.blackbox

import com.google.common.truth.Truth.assertThat
import com.zendesk.connect.resetConnect
import com.zendesk.connect.testInitConnect
import com.zendesk.connect.testapp.helpers.clearFiles
import com.zendesk.connect.testapp.helpers.clearSharedPrefs
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RequestsVerifier.verifyRequest
import io.appflate.restmock.utils.RequestMatchers.pathContains
import io.appflate.restmock.utils.RequestMatchers.pathEndsWith
import io.outbound.sdk.Outbound
import io.outbound.sdk.testInitOutbound
import org.junit.Before
import org.junit.Test

/**
 *
 * Black box tests for SDK behaviour when the SDK is successfully initialised and config is enabled.
 *
 * There are some unusual issues that arose while writing these tests:
 * <ul>
 *     <li>The config retrieved from requests is stored in SharedPreferences and needs to be cleared
 *     between tests so that the init call will always result in a config request</li>
 *
 *     <li>Requests like track and identify are stored in a database until they are due to be
 *     processed. This will cause issues when a test fails, causing subsequent tests to fail because
 *     too many network invocations were detected when a test should have passed. To handle this,
 *     the database must be cleared between tests</li>
 *
 *     <li>Calling non-init methods before init should result in IllegalStateExceptions but they
 *     actually produce NullPointerExceptions because the WorkerHandler is not created at that point.</li>
 *
 *     <li>Event though storage is cleared between tests, the extra threads spun up may not stop
 *     between tests. This means that we will encounter the IllegalStateException instead of the
 *     NullPointerException for any tests that run after tests which successfully call init. These
 *     tests have been extracted into AnyExceptionTests so that they can run first</li>
 * </ul>
 */
class ConfigEnabledTests {

    @Before
    fun setup() {
        resetConnect()
        RESTMockServer.reset()
        clearSharedPrefs()
        clearFiles()

        RESTMockServer.whenGET(pathContains(configPath))
                .thenReturnFile(200, "config_enabled_response.json")

        RESTMockServer.whenPOST(pathEndsWith(identifyPath))
                .thenReturnEmpty(200)

        testInitConnect(testClient) // This is done internally by Outbound
        testInitOutbound(testApplication, "Whatever", "Whatevs", testClient)
    }

    @Test
    fun callingIdentifyUserShouldMakeAnIdentifyRequestToTheApi() {
        Outbound.identify(testUser)

        verifyRequest(pathEndsWith(identifyPath)).invoked()
    }

    @Test
    fun callingTrackEventShouldMakeATrackRequestToTheApi() {
        RESTMockServer.whenPOST(pathEndsWith(trackPath))
                .thenReturnEmpty(200)

        Outbound.identify(testUser)

        Outbound.track(testEvent)

        verifyRequest(pathEndsWith(trackPath)).invoked()
    }

    @Test
    fun callingRegisterForPushShouldMakeARegisterRequestToTheApi() {
        RESTMockServer.whenPOST(pathEndsWith(registerPath))
                .thenReturnEmpty(200)

        Outbound.identify(testUser)

        Outbound.register()

        Thread.sleep(50) // tiny delay just to let async ops catch up

        verifyRequest(pathEndsWith(registerPath)).invoked()
    }

    @Test
    fun callingDisablePushNotificationsShouldMakeADisableRequestToTheApi() {
        RESTMockServer.whenPOST(pathEndsWith(disablePath))
                .thenReturnEmpty(200)

        Outbound.identify(testUser)

        Outbound.disable()

        Thread.sleep(50) // tiny delay just to let async ops catch up

        verifyRequest(pathEndsWith(disablePath)).invoked()
    }

    @Test
    fun callingGetActiveTokenShouldReturnANonEmptyStringIfAUserIsIdentified() {
        Outbound.identify(testUser)

        verifyRequest(pathEndsWith(identifyPath)).invoked()

        assertThat(Outbound.getActiveToken()).isNotEmpty()
    }

    @Test
    fun callingPairDeviceWithAValidPinShouldReturnTrue() {
        RESTMockServer.whenPOST(pathEndsWith(pairPath))
                .thenReturnEmpty(200)

        assertThat(Outbound.pairDevice("9999")).isTrue()

        verifyRequest(pathEndsWith(pairPath)).invoked()
    }

    @Test
    fun callingPairDeviceWithAnInvalidPinShouldReturnFalse() {
        RESTMockServer.whenPOST(pathEndsWith(pairPath))
                .thenReturnEmpty(401)

        assertThat(Outbound.pairDevice("9999")).isFalse()

        verifyRequest(pathEndsWith(pairPath)).invoked()
    }

}
