package com.zendesk.connect

import android.support.test.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.zendesk.connect.testapp.blackbox.testClient
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RESTMockServerStarter
import io.appflate.restmock.android.AndroidAssetsFileParser
import io.appflate.restmock.android.AndroidLogger
import org.junit.After
import org.junit.Before
import org.junit.Test

class ConnectTests {

    @Before
    fun setUp() {
        RESTMockServerStarter.startSync(AndroidAssetsFileParser(InstrumentationRegistry.getContext()), AndroidLogger())
    }

    @After
    fun tearDown() {
        RESTMockServer.reset()
    }

    @Test
    fun isInitialisedShouldReturnTrueIfSdkIsInitialised() {
        testInitConnect(testClient)

        assertThat(Connect.INSTANCE.isInitialised).isTrue()
    }

    @Test
    fun isInitialisedShouldReturnFalseIfSdkIsNotInitialised() {
        assertThat(Connect.INSTANCE.isInitialised).isFalse()
    }
}