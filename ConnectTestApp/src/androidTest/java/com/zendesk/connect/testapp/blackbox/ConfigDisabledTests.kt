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
import io.appflate.restmock.RequestsVerifier.verifyRequest
import io.appflate.restmock.utils.RequestMatchers
import io.appflate.restmock.utils.RequestMatchers.pathEndsWith
import io.outbound.sdk.Outbound
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString

/**
 * Black box tests for SDK behaviour when the SDK if initialised and config is disabled,
 * which should result in the SDK not making any network requests.
 */
class ConfigDisabledTests {

    @get:Rule
    private val testRule = ActivityTestRule(MainActivity::class.java, true, false)

    @Before
    fun setUp() {
        RESTMockServer.reset()
        clearSharedPrefs()
        clearDatabase()

        testRule.launchActivity(null)

        testRule.activity.testUrl = RESTMockServer.getUrl()

        RESTMockServer.whenGET(RequestMatchers.pathContains(configPath))
                .thenReturnFile(200, "config_disabled_response.json")

        onView(withId(R.id.init_sdk_button)).perform(click())
    }

    @Test
    fun callingIdentifyUserWithADisabledConfigShouldMakeNoRequestToTheApi() {
        onView(withId(R.id.identify_user_button)).perform(click())

        verifyRequest(pathEndsWith(identifyPath)).never()
    }

    @Test
    fun callingTrackEventWithADisabledConfigShouldMakeNoRequestToTheApi() {
        onView(withId(R.id.track_event_button)).perform(click())

        verifyRequest(pathEndsWith(trackPath)).never()
    }

    @Test
    fun callingRegisterForPushWithADisabledConfigShouldMakeNoRequestToTheApi() {
        onView(withId(R.id.register_button)).perform(click())

        verifyRequest(pathEndsWith(registerPath)).never()
    }

    @Test
    fun callingDisablePushNotificationsWithADisabledConfigShouldMakeNoRequestToTheApi() {
        onView(withId(R.id.disable_button)).perform(click())

        verifyRequest(pathEndsWith(disablePath)).never()
    }

    @Test
    fun pairDeviceShouldReturnFalseIfConfigIsDisabled() {
        assertThat(Outbound.pairDevice(anyString())).isFalse()
    }

}