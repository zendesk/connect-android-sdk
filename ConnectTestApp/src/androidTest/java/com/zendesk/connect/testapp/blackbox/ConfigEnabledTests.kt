package com.zendesk.connect.testapp.blackbox

import android.support.test.espresso.IdlingRegistry
import android.support.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import com.zendesk.connect.testapp.MainActivity
import com.zendesk.connect.testapp.helpers.clearDatabase
import com.zendesk.connect.testapp.helpers.clearSharedPrefs
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RequestsVerifier
import io.appflate.restmock.utils.RequestMatchers.pathContains
import io.appflate.restmock.utils.RequestMatchers.pathEndsWith
import io.outbound.sdk.initSdkForTesting
import io.outbound.sdk.Outbound
import org.junit.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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

    @get:Rule
    private val testRule = ActivityTestRule(MainActivity::class.java, true, false)

    private lateinit var latch: CountDownLatch

    @Before
    fun setup() {
        RESTMockServer.reset()
        clearDatabase()
        clearSharedPrefs()

        testRule.launchActivity(null)

        latch = CountDownLatch(3) // init, identify, some other action

        IdlingRegistry.getInstance().register(idlingClient)

        idlingClient.registerIdleTransitionCallback { latch.countDown() }

        RESTMockServer.whenGET(pathContains(configPath))
                .thenReturnFile(200, "config_enabled_response.json")

        RESTMockServer.whenPOST(pathEndsWith(identifyPath))
                .thenReturnEmpty(200)

        initSdkForTesting(testRule.activity.application, "Whatever",
                "Whatevs", testClient)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(idlingClient)
    }

    @Test
    fun callingIdentifyUserShouldMakeAnIdentifyRequestToTheApi() {
        Outbound.identify(testUser)

        latch = CountDownLatch(2) // init, identify

        latch.await(2, TimeUnit.SECONDS)

        RequestsVerifier.verifyRequest(pathEndsWith(identifyPath)).exactly(1)
    }

    @Test
    fun callingTrackEventShouldMakeATrackRequestToTheApi() {
        RESTMockServer.whenPOST(pathEndsWith(trackPath))
                .thenReturnEmpty(200)

        Outbound.identify(testUser)
        Outbound.track(testEvent)

        latch.await(2, TimeUnit.SECONDS)

        RequestsVerifier.verifyRequest(pathEndsWith(trackPath)).invoked()
    }

    @Test
    fun callingRegisterForPushShouldMakeARegisterRequestToTheApi() {
        RESTMockServer.whenPOST(pathEndsWith(registerPath))
                .thenReturnEmpty(200)

        Outbound.identify(testUser)
        Outbound.register()

        latch.await(2, TimeUnit.SECONDS)

        RequestsVerifier.verifyRequest(pathEndsWith(registerPath)).invoked()
    }

    @Test
    fun callingDisablePushNotificationsShouldMakeADisableRequestToTheApi() {
        RESTMockServer.whenPOST(pathEndsWith(disablePath))
                .thenReturnEmpty(200)

        Outbound.identify(testUser)
        Outbound.disable()

        latch.await(2, TimeUnit.SECONDS)

        RequestsVerifier.verifyRequest(pathEndsWith(disablePath)).invoked()
    }

    @Test
    fun callingDisablePushNotificationsWithNoUserIdentifiedShouldMakeNoRequestToTheApi() {
        Outbound.disable()

        RequestsVerifier.verifyRequest(pathEndsWith(disablePath)).never()
    }

    @Test
    fun callingGetActiveTokenShouldReturnANonEmptyStringIfAUserIsIdentified() {
        latch = CountDownLatch(2)

        Outbound.identify(testUser)

        latch.await(2, TimeUnit.SECONDS)

        assertThat(Outbound.getActiveToken()).isNotEmpty()
    }

    @Test
    fun callingGetActiveTokenShouldReturnAnEmptyStringIfNoUserIsIdentified() {
        assertThat(Outbound.getActiveToken()).isEmpty()
    }

    @Test
    fun callingPairDeviceWithAValidPinShouldReturnTrue() {
        RESTMockServer.whenPOST(pathEndsWith(pairPath))
                .thenReturnEmpty(200)

        assertThat(Outbound.pairDevice("9999")).isTrue()
    }

    @Test
    fun callingPairDeviceWithAnInvalidPinShouldReturnFalse() {
        RESTMockServer.whenPOST(pathEndsWith(pairPath))
                .thenReturnEmpty(401)

        assertThat(Outbound.pairDevice("9999")).isFalse()
    }

}
