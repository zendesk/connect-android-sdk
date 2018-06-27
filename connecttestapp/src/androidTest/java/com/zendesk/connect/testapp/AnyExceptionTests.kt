package com.zendesk.connect.testapp

import android.support.test.rule.ActivityTestRule
import io.outbound.sdk.Event
import io.outbound.sdk.Outbound
import io.outbound.sdk.User
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString

/**
 * Tests any exceptions expected by using the SDK non-init methods before initialisation.
 *
 * The tests that expect NullPointerException must run first due to poor thread handling in the SDK.
 * Tests which successfully call init will result in the WorkerThread existing leading to an
 * IllegalStateException rather than a NullPointerException. Simple ensuring that this file is first
 * alphabetically will cause the tests to run first.
 *
 * The tests for getActiveToken and pairDevice aren't dependent on the WorkerThread but I've
 * included them here to keep them together with the other exception tests.
 */
class AnyExceptionTests {

    @get:Rule
    private val testRule = ActivityTestRule(MainActivity::class.java, true, false)

    @Before
    fun setUp() {
        testRule.launchActivity(null)
    }

    @After
    fun tearDown() {

    }

    @Test(expected = NullPointerException::class)
    fun callingIdentifyBeforeInitShouldRaiseAnException() {
        val user = User.Builder()
                .setUserId("test_123")
                .setFirstName("Dan")
                .build()
        Outbound.identify(user)
    }

    @Test(expected = NullPointerException::class)
    fun callingTrackEventBeforeInitShouldRaiseAnException() {
        Outbound.track(Event("Test Event"))
    }

    @Test(expected = NullPointerException::class)
    fun callingRegisterForPushBeforeInitShouldRaiseAnException() {
        Outbound.register()
    }

    @Test(expected = NullPointerException::class)
    fun callingDisablePushNotificationsBeforeInitShouldRaiseAnException() {
        Outbound.disable()
    }

    @Test(expected = NullPointerException::class)
    fun callingLogoutBeforeInitShouldRaiseAnException() {
        Outbound.logout()
    }

    @Test(expected = IllegalStateException::class)
    fun callingGetActiveTokenBeforeInitShouldRaiseAnException() {
        Outbound.getActiveToken()
    }

    @Test(expected = IllegalStateException::class)
    fun callingPairDeviceBeforeInitShouldRaiseAnException() {
        Outbound.pairDevice(anyString())
    }
}