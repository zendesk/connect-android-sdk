package com.zendesk.connect.testapp.blackbox

import com.zendesk.connect.resetConnect
import com.zendesk.connect.testInitConnect
import com.zendesk.connect.testapp.helpers.clearFiles
import com.zendesk.connect.testapp.helpers.clearSharedPrefs
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RequestsVerifier.verifyRequest
import io.appflate.restmock.utils.RequestMatchers.pathContains
import io.outbound.sdk.testInitOutbound
import org.junit.Before
import org.junit.Test

/**
 * Black box testing the behaviour relating to initialisation of the SDK
 */
class InitialisationTests {

    @Before
    fun setup() {
        resetConnect()
        RESTMockServer.reset()
        clearSharedPrefs()
        clearFiles()

        RESTMockServer.whenGET(pathContains(configPath))
                .thenReturnFile(200, "config_enabled_response.json")
    }

    @Test
    fun callingInitShouldRetrieveAConfigFromTheApi() {
        testInitConnect(testClient) // This is done internally by Outbound
        testInitOutbound(testApplication, "Whatever", "Whatevs", testClient)

        verifyRequest(pathContains(configPath)).invoked()
    }

    @Test
    fun callingInitMoreThanOnceShouldOnlyResultInASingleConfigRequestToTheApi() {
        testInitConnect(testClient) // This is done internally by Outbound
        testInitOutbound(testApplication, "Whatever", "Whatevs", testClient)

        testInitConnect(testClient) // This is done internally by Outbound
        testInitOutbound(testApplication, "Whatever", "Whatevs", testClient)

        verifyRequest(pathContains(configPath)).invoked()
    }
}