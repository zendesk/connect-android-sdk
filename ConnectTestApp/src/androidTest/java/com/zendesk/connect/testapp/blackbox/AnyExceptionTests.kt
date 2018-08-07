package com.zendesk.connect.testapp.blackbox

import android.support.test.rule.ActivityTestRule
import com.zendesk.connect.testapp.MainActivity
import com.zendesk.connect.testapp.helpers.clearDatabase
import com.zendesk.connect.testapp.helpers.clearSharedPrefs
import io.outbound.sdk.Event
import io.outbound.sdk.Outbound
import io.outbound.sdk.User
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString

/**
 * Tests any exceptions expected by using the SDK non-init methods before initialisation.
 *
 * Even though storage is cleared between tests, the extra threads spun up may not stop
 * between tests. This means that we will encounter an IllegalStateException instead of a
 * NullPointerException for any tests that run after tests which successfully call init.
 *
 * None of the tests in this file should result in successful initialisations so ensuring that
 * this file is first alphabetically will cause the tests to run before any initialisations.
 *
 * getActiveToken and pairDevice aren't dependent on the WorkerThread so they will throw IllegalStateException.
 */
class AnyExceptionTests {

    @get:Rule
    private val testRule = ActivityTestRule(MainActivity::class.java, true, false)

    @Before
    fun setUp() {
        clearSharedPrefs()
        clearDatabase()

        testRule.launchActivity(null)
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