package com.zendesk.connect.testapp.blackbox

import android.support.test.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.zendesk.connect.Connect
import com.zendesk.connect.resetConnect
import com.zendesk.connect.storeEnabledConfig
import com.zendesk.connect.testInitConnect
import com.zendesk.connect.testapp.helpers.clearFiles
import com.zendesk.connect.testapp.helpers.clearSharedPrefs
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RESTMockServerStarter
import io.appflate.restmock.RequestsVerifier.verifyRequest
import io.appflate.restmock.android.AndroidAssetsFileParser
import io.appflate.restmock.android.AndroidLogger
import io.appflate.restmock.utils.RequestMatchers.pathContains
import io.appflate.restmock.utils.RequestMatchers.pathEndsWith
import org.junit.After
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

    init {
        RESTMockServerStarter.startSync(
                AndroidAssetsFileParser(InstrumentationRegistry.getTargetContext()),
                AndroidLogger()
        )

        RESTMockServer.reset()

        RESTMockServer.whenGET(pathContains(configPath))
                .thenReturnFile(200, "config_enabled_response.json")

        RESTMockServer.whenPOST(pathEndsWith(identifyPath))
                .thenReturnEmpty(200)

        RESTMockServer.whenPOST(pathEndsWith(trackPath))
                .thenReturnEmpty(200)

        RESTMockServer.whenPOST(pathEndsWith(registerPath))
                .thenReturnEmpty(200)

        RESTMockServer.whenPOST(pathEndsWith(disablePath))
                .thenReturnEmpty(200)
    }

    @Before
    fun setup() {
        testInitConnect(testClient, shouldMakeConfigCall = false)
        storeEnabledConfig()
    }

    @After
    fun tearDown() {
        clearSharedPrefs()
        clearFiles()
        resetConnect()
    }

    @Test
    fun callingIdentifyUserShouldMakeAnIdentifyRequestToTheApi() {
        Connect.INSTANCE.identifyUser(testUser)

        verifyRequest(pathEndsWith(identifyPath)).invoked()
    }

    @Test
    fun callingTrackEventShouldMakeATrackRequestToTheApi() {
        Connect.INSTANCE.identifyUser(testUser)

        Connect.INSTANCE.trackEvent(testEvent)

        verifyRequest(pathEndsWith(trackPath)).invoked()
    }

    @Test
    fun callingRegisterForPushShouldMakeARegisterRequestToTheApi() {
        Connect.INSTANCE.identifyUser(testUser)

        Connect.INSTANCE.registerForPush()

        Thread.sleep(50) // tiny delay just to let async ops catch up

        verifyRequest(pathEndsWith(registerPath)).invoked()
    }

    @Test
    fun callingDisablePushNotificationsShouldMakeADisableRequestToTheApi() {
        Connect.INSTANCE.identifyUser(testUser)

        Connect.INSTANCE.disablePush()

        Thread.sleep(50) // tiny delay just to let async ops catch up

        verifyRequest(pathEndsWith(disablePath)).invoked()
    }

    @Test
    fun callingGetActiveTokenShouldReturnANonEmptyStringIfAUserIsIdentified() {
        Connect.INSTANCE.identifyUser(testUser)

        verifyRequest(pathEndsWith(identifyPath)).invoked()

        val tokens = Connect.INSTANCE.user.fcm

        assertThat(tokens).isNotEmpty()
        assertThat(tokens[0]).isNotEmpty()
    }

}
