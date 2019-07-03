package com.zendesk.connect.testapp.blackbox

import android.support.test.InstrumentationRegistry
import com.zendesk.connect.resetConnect
import com.zendesk.connect.testInitConnect
import com.zendesk.connect.testapp.helpers.clearFiles
import com.zendesk.connect.testapp.helpers.clearSharedPrefs
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RESTMockServerStarter
import io.appflate.restmock.RequestsVerifier.verifyRequest
import io.appflate.restmock.android.AndroidAssetsFileParser
import io.appflate.restmock.android.AndroidLogger
import io.appflate.restmock.utils.RequestMatchers.pathContains
import org.junit.Before
import org.junit.Test

/**
 * Black box testing the behaviour relating to initialisation of the SDK
 */
class InitialisationTests {

    init {
        RESTMockServerStarter.startSync(
                AndroidAssetsFileParser(InstrumentationRegistry.getTargetContext()),
                AndroidLogger()
        )

        RESTMockServer.reset()

        RESTMockServer.whenGET(pathContains(configPath))
                .thenReturnFile(200, "config_enabled_response.json")

        clearSharedPrefs()
        clearFiles()
    }

    @Before
    fun setup() {
        resetConnect()
    }

    @Test
    fun callingInitShouldRetrieveAConfigFromTheApi() {
        testInitConnect(testClient)

        verifyRequest(pathContains(configPath)).invoked()
    }

    @Test
    fun callingInitMoreThanOnceShouldOnlyResultInASingleConfigRequestToTheApi() {
        testInitConnect(testClient)

        testInitConnect(testClient)

        verifyRequest(pathContains(configPath)).invoked()
    }
}
