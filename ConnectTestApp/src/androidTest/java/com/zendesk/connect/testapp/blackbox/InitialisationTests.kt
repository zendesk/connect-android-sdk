package com.zendesk.connect.testapp.blackbox

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import com.zendesk.connect.testapp.MainActivity
import com.zendesk.connect.testapp.R
import com.zendesk.connect.testapp.helpers.clearDatabase
import com.zendesk.connect.testapp.helpers.clearSharedPrefs
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RequestsVerifier.verifyRequest
import io.appflate.restmock.utils.RequestMatchers.pathContains
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Black box testing the behaviour relating to initialisation of the SDK
 */
class InitialisationTests {

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
    }

    @Test
    fun callingInitShouldRetrieveAConfigFromTheApi() {
        onView(withId(R.id.init_sdk_button)).perform(click())

        verifyRequest(pathContains(configPath)).invoked()
    }

    @Test
    fun callingInitMoreThanOnceShouldOnlyResultInASingleConfigRequestToTheApi() {
        onView(withId(R.id.init_sdk_button)).perform(click())
        onView(withId(R.id.init_sdk_button)).perform(click())

        verifyRequest(pathContains(configPath)).exactly(1)
    }
}