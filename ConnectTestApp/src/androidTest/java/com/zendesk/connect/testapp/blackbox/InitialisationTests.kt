package com.zendesk.connect.testapp.blackbox

import android.support.test.espresso.IdlingRegistry
import android.support.test.rule.ActivityTestRule
import com.zendesk.connect.testapp.MainActivity
import com.zendesk.connect.testapp.helpers.clearDatabase
import com.zendesk.connect.testapp.helpers.clearSharedPrefs
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RequestsVerifier.verifyRequest
import io.appflate.restmock.utils.RequestMatchers.pathContains
import io.outbound.sdk.initSdkForTesting
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Black box testing the behaviour relating to initialisation of the SDK
 */
class InitialisationTests {

    @get:Rule
    private val testRule = ActivityTestRule(MainActivity::class.java, true, false)

    private lateinit var latch: CountDownLatch

    @Before
    fun setup() {
        RESTMockServer.reset()
        clearDatabase()
        clearSharedPrefs()

        testRule.launchActivity(null)

        latch = CountDownLatch(1) // init

        IdlingRegistry.getInstance().register(idlingClient)

        idlingClient.registerIdleTransitionCallback { latch.countDown() }

        RESTMockServer.whenGET(pathContains(configPath))
                .thenReturnFile(200, "config_enabled_response.json")
    }

    @Test
    fun callingInitShouldRetrieveAConfigFromTheApi() {
        initSdkForTesting(testRule.activity.application, "Whatever",
                "Whatevs", testClient)

        latch.await(2, TimeUnit.SECONDS)

        verifyRequest(pathContains(configPath)).invoked()
    }

    @Test
    fun callingInitMoreThanOnceShouldOnlyResultInASingleConfigRequestToTheApi() {
        initSdkForTesting(testRule.activity.application, "Whatever",
                "Whatevs", testClient)

        // Wait for the config to be loaded before trying to call init again
        latch.await(2, TimeUnit.SECONDS)

        initSdkForTesting(testRule.activity.application, "Whatever",
                "Whatevs", testClient)

        verifyRequest(pathContains(configPath)).exactly(1)
    }
}