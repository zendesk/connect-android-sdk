package com.zendesk.connect.testapp.blackbox

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import com.zendesk.connect.testapp.MainActivity
import com.zendesk.connect.testapp.R
import com.zendesk.connect.testapp.helpers.clearDatabase
import com.zendesk.connect.testapp.helpers.clearSharedPrefs
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RequestsVerifier
import io.appflate.restmock.utils.RequestMatchers.pathContains
import io.appflate.restmock.utils.RequestMatchers.pathEndsWith
import io.outbound.sdk.Outbound
import org.junit.*

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

    @Before
    fun setup() {
        RESTMockServer.reset()
        clearDatabase()
        clearSharedPrefs()

        testRule.launchActivity(null)

        testRule.activity.testUrl = RESTMockServer.getUrl()

        RESTMockServer.whenGET(pathContains(configPath))
                .thenReturnFile(200, "config_enabled_response.json")

        onView(withId(R.id.init_sdk_button)).perform(click())
    }

    @Test
    fun callingIdentifyUserShouldMakeAnIdentifyRequestToTheApi() {
        RESTMockServer.whenPOST(pathEndsWith(identifyPath))
                .thenReturnEmpty(200)

        onView(withId(R.id.identify_user_button)).perform(click())

        RequestsVerifier.verifyRequest(pathEndsWith(identifyPath)).invoked()
    }

    @Test
    fun callingTrackEventShouldMakeATrackRequestToTheApi() {
        RESTMockServer.whenPOST(pathEndsWith(trackPath))
                .thenReturnEmpty(200)

        onView(withId(R.id.track_event_button)).perform(click())

        RequestsVerifier.verifyRequest(pathEndsWith(trackPath)).invoked()
    }

    @Test
    fun callingRegisterForPushShouldMakeARegisterRequestToTheApi() {
        RESTMockServer.whenPOST(pathEndsWith(registerPath))
                .thenReturnEmpty(200)

        onView(withId(R.id.register_button)).perform(click())

        RequestsVerifier.verifyRequest(pathEndsWith(registerPath)).invoked()
    }

    @Test
    fun callingDisablePushNotificationsShouldMakeADisableRequestToTheApi() {
        RESTMockServer.whenPOST(pathEndsWith(disablePath))
                .thenReturnEmpty(200)

        //Need to identify the user first
        onView(withId(R.id.identify_user_button)).perform(click())
        onView(withId(R.id.disable_button)).perform(click())

        RequestsVerifier.verifyRequest(pathEndsWith(disablePath)).invoked()
    }

    @Test
    fun callingDisablePushNotificationsWithNoUserIdentifiedShouldMakeNoRequestToTheApi() {
        onView(withId(R.id.disable_button)).perform(click())

        RequestsVerifier.verifyRequest(pathEndsWith(disablePath)).never()
    }

    @Test
    fun callingGetActiveTokenShouldReturnANonEmptyStringIfAUserIsIdentified() {
        onView(withId(R.id.identify_user_button)).perform(click())

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
