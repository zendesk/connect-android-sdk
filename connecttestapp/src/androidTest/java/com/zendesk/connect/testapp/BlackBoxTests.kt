package com.zendesk.connect.testapp

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RequestsVerifier
import io.appflate.restmock.utils.RequestMatchers.pathContains
import io.appflate.restmock.utils.RequestMatchers.pathEndsWith
import org.junit.*

/**
 * Black box instrumented tests for ensuring the correct outputs for given inputs to the system.
 * The internals of the existing SDK are tightly coupled and inflexible when it comes to testing so
 * these black box tests have been created to ensure that any refactoring won't compromise the
 * operation of the SDK as it is right now.
 *
 * There are some unusual issues that arose while writing these tests which will be documented here.
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
class BlackBoxTests {

    @get:Rule
    private val testRule = ActivityTestRule(MainActivity::class.java, true, false)

    private val configPath = "/i/config/sdk/android"
    private val identifyPath = "/v2/identify"
    private val trackPath = "/v2/track"
    private val registerPath = "/v2/gcm/register"
    private val disablePath = "/v2/gcm/disable"

    @Before
    fun setup() {
        RESTMockServer.reset()

        testRule.launchActivity(null)

        testRule.activity.testUrl = RESTMockServer.getUrl()

        clearDatabase()
        clearSharedPrefs()

        RESTMockServer.whenGET(pathContains(configPath))
                .thenReturnFile(200, "config_enabled_response.json")
    }

    @After
    fun tearDown() {

    }

    ///CONFIG TESTS////

    @Test
    fun callingInitShouldRetrieveAConfigFromTheApi() {
        onView(withId(R.id.init_sdk_button)).perform(click())

        RequestsVerifier.verifyRequest(pathContains(configPath)).invoked()
    }

    @Test
    fun callingInitMoreThanOnceShouldOnlyResultInASingleConfigRequestToTheApi() {
        onView(withId(R.id.init_sdk_button)).perform(click())
        onView(withId(R.id.init_sdk_button)).perform(click())

        RequestsVerifier.verifyRequest(pathContains(configPath)).exactly(1)
    }

    ///IDENTIFY TESTS///

    @Test
    fun callingIdentifyUserShouldMakeAnIdentifyRequestToTheApi() {
        RESTMockServer.whenPOST(pathEndsWith(identifyPath))
                .thenReturnEmpty(200)

        onView(withId(R.id.init_sdk_button)).perform(click())
        onView(withId(R.id.identify_user_button)).perform(click())

        RequestsVerifier.verifyRequest(pathContains(configPath)).invoked()
        RequestsVerifier.verifyRequest(pathEndsWith(identifyPath)).invoked()
    }

    @Test
    fun callingIdentifyUserWithADisabledConfigShouldMakeNoRequestToTheApi() {
        RESTMockServer.reset() //Resetting to clear the config_enabled_response

        RESTMockServer.whenGET(pathContains(configPath))
                .thenReturnFile(200, "config_disabled_response.json")

        onView(withId(R.id.init_sdk_button)).perform(click())
        onView(withId(R.id.identify_user_button)).perform(click())

        RequestsVerifier.verifyRequest(pathContains(configPath)).invoked()
        RequestsVerifier.verifyRequest(pathEndsWith(identifyPath)).never()
    }

    ///TRACKING TESTS///

    @Test
    fun callingTrackEventShouldMakeATrackRequestToTheApi() {
        RESTMockServer.whenPOST(pathEndsWith(trackPath))
                .thenReturnEmpty(200)

        onView(withId(R.id.init_sdk_button)).perform(click())
        onView(withId(R.id.track_event_button)).perform(click())

        RequestsVerifier.verifyRequest(pathEndsWith(trackPath)).invoked()
    }

    @Test
    fun callingTrackEventWithADisabledConfigShouldMakeNoRequestToTheApi() {
        RESTMockServer.reset() //Resetting to clear the config_enabled_response

        RESTMockServer.whenGET(pathContains(configPath))
                .thenReturnFile(200, "config_disabled_response.json")

        onView(withId(R.id.init_sdk_button)).perform(click())
        onView(withId(R.id.track_event_button)).perform(click())

        RequestsVerifier.verifyRequest(pathContains(configPath)).invoked()
        RequestsVerifier.verifyRequest(pathEndsWith(trackPath)).never()
    }

    ///REGISTER TESTS///

    @Test
    fun callingRegisterForPushShouldMakeARegisterRequestToTheApi() {
        RESTMockServer.whenPOST(pathEndsWith(registerPath))
                .thenReturnEmpty(200)

        onView(withId(R.id.init_sdk_button)).perform(click())
        onView(withId(R.id.register_button)).perform(click())

        RequestsVerifier.verifyRequest(pathEndsWith(registerPath)).invoked()
    }

    @Test
    fun callingRegisterForPushWithADisabledConfigShouldMakeNoRequestToTheApi() {
        RESTMockServer.reset() //Resetting to clear the config_enabled_response

        RESTMockServer.whenGET(pathContains(configPath))
                .thenReturnFile(200, "config_disabled_response.json")

        onView(withId(R.id.init_sdk_button)).perform(click())
        onView(withId(R.id.register_button)).perform(click())

        RequestsVerifier.verifyRequest(pathContains(configPath)).invoked()
        RequestsVerifier.verifyRequest(pathEndsWith(registerPath)).never()
    }

    ///DISABLE TESTS///

    @Test
    fun callingDisablePushNotificationsShouldMakeADisableRequestToTheApi() {
        RESTMockServer.whenPOST(pathEndsWith(disablePath))
                .thenReturnEmpty(200)

        onView(withId(R.id.init_sdk_button)).perform(click())
        onView(withId(R.id.identify_user_button)).perform(click())
        onView(withId(R.id.disable_button)).perform(click())

        RequestsVerifier.verifyRequest(pathEndsWith(disablePath)).invoked()
    }

    @Test
    fun callingDisablePushNotificationsWithADisabledConfigShouldMakeNoRequestToTheApi() {
        RESTMockServer.reset() //Resetting to clear the config_enabled_response

        RESTMockServer.whenGET(pathContains(configPath))
                .thenReturnFile(200, "config_disabled_response.json")

        onView(withId(R.id.init_sdk_button)).perform(click())
        onView(withId(R.id.disable_button)).perform(click())

        RequestsVerifier.verifyRequest(pathContains(configPath)).invoked()
        RequestsVerifier.verifyRequest(pathEndsWith(disablePath)).never()
    }

    ///HELPERS///

    /**
     * Clear any Outbound SDK data stored in SharedPrefs on the device
     */
    private fun clearSharedPrefs() {
        val instrumentation = getInstrumentation()
        val sharedPreferences = instrumentation.targetContext
                .getSharedPreferences("io.outbound.sdk.prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().commit()
    }

    /**
     * Deletes the database created in OutboundStorage
     */
    private fun clearDatabase() {
        InstrumentationRegistry.getTargetContext().deleteDatabase("io.outbound.sdk")
    }

}
