package com.zendesk.connect.testapp.blackbox

import com.zendesk.connect.testapp.helpers.clearFiles
import com.zendesk.connect.testapp.helpers.clearSharedPrefs
import io.outbound.sdk.Event
import io.outbound.sdk.Outbound
import io.outbound.sdk.User
import org.junit.Before
import org.junit.Test

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

    @Before
    fun setUp() {
        clearSharedPrefs()
        clearFiles()
    }

    @Test(expected = IllegalStateException::class)
    fun callingIdentifyBeforeInitShouldRaiseAnException() {
        val user = User.Builder()
                .setUserId("test_123")
                .setFirstName("Dan")
                .build()
        Outbound.identify(user)
    }

    @Test(expected = IllegalStateException::class)
    fun callingTrackEventBeforeInitShouldRaiseAnException() {
        Outbound.track(Event("Test Event"))
    }

    @Test(expected = IllegalStateException::class)
    fun callingRegisterForPushBeforeInitShouldRaiseAnException() {
        Outbound.register()
    }

    @Test(expected = IllegalStateException::class)
    fun callingDisablePushNotificationsBeforeInitShouldRaiseAnException() {
        Outbound.disable()
    }

    @Test(expected = IllegalStateException::class)
    fun callingLogoutBeforeInitShouldRaiseAnException() {
        Outbound.logout()
    }

    @Test(expected = IllegalStateException::class)
    fun callingGetActiveTokenBeforeInitShouldRaiseAnException() {
        Outbound.getActiveToken()
    }

    @Test(expected = IllegalStateException::class)
    fun callingPairDeviceBeforeInitShouldRaiseAnException() {
        Outbound.pairDevice("0000")
    }
}