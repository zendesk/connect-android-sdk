package com.zendesk.connect

import android.support.test.InstrumentationRegistry
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.InstanceIdResult
import com.google.gson.Gson
import io.appflate.restmock.RESTMockServer
import okhttp3.OkHttpClient
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.concurrent.TimeUnit

/**
 * Initialises the Connect SDK using a test [ConnectComponent]. Allows us to inject a testing
 * [OkHttpClient] so we can redirect network requests to [RESTMockServer] to verify the
 * endpoints being hit.
 */
internal fun testInitConnect(testClient: OkHttpClient) {
    Connect.INSTANCE.init(getTestComponent(testClient))
}

/**
 * Resets the [Connect] instance so that the SDK will retrieve a config on init. We do a check to
 * see if the init is happening on a cold start of the SDK so we can skip config requests if not
 * needed, but this needs to be avoided for tests.
 */
internal fun resetConnect() {
    Connect.INSTANCE.reset()
}

/**
 * Injects a test [ConnectComponent] into [Connect.init] with the following overrides:
 * * Each network provider uses the [RESTMockServer] url
 * * Provides a mock [ConnectScheduler] to avoid FirebaseJobDispatcher which we cannot mock
 * * Provides the given test [OkHttpClient] as the base client
 * * Provides a mock [ConnectInstanceId] to provide tokens with Firebase
 */
internal fun getTestComponent(testClient: OkHttpClient): ConnectComponent {
    val apiKey = "auth_me_plz"
    val baseUrl = RESTMockServer.getUrl()
    return DaggerConnectComponent.builder()
            .connectModule(getTestConnectModule())
            .connectNetworkModule(getTestConnectNetworkModule(apiKey, baseUrl, testClient))
            .connectStorageModule(ConnectStorageModule(InstrumentationRegistry.getTargetContext()))
            .build()
}

/**
 * Provides a [ConnectModule] with any overrides needed for testing
 */
internal fun getTestConnectModule(): ConnectModule {
    return object: ConnectModule(InstrumentationRegistry.getContext()) {
        override fun provideConnectScheduler(): ConnectScheduler {
            return getMockScheduler()
        }

        override fun provideConnectInstanceId(): ConnectInstanceId {
            val mockIdResult = Mockito.mock(InstanceIdResult::class.java)
            `when`(mockIdResult.token).thenReturn("dummy_token")

            val mockInstanceId = Mockito.mock(ConnectInstanceId::class.java)
            `when`(mockInstanceId.getToken(any<OnSuccessListener<InstanceIdResult>>(), any<OnFailureListener>()))
                    .then {
                        (it.arguments[0] as OnSuccessListener<InstanceIdResult>).onSuccess(mockIdResult)
                    }

            return mockInstanceId
        }
    }
}

/**
 * Provides a [ConnectNetworkModule] with any overrides needed for testing
 */
internal fun getTestConnectNetworkModule(apiKey: String, baseUrl: String, testClient: OkHttpClient): ConnectNetworkModule {
    return object: ConnectNetworkModule(apiKey) {
        override fun provideConfigProvider(client: OkHttpClient?, gson: Gson?): ConfigProvider {
            return ConfigProviderImpl(client, baseUrl, gson)
        }

        override fun provideEventProvider(client: OkHttpClient?, gson: Gson?): EventProvider {
            return EventProviderImpl(client, baseUrl, gson)
        }

        override fun provideIdentifyProvider(client: OkHttpClient?, gson: Gson?): IdentifyProvider {
            return IdentifyProviderImpl(client, baseUrl, gson)
        }

        override fun providePushProvider(client: OkHttpClient?, gson: Gson?): PushProvider {
            return PushProviderImpl(client, baseUrl, gson)
        }

        override fun provideBaseOkHttpClient(clientInterceptor: ClientInterceptors.OutboundClientInterceptor?,
                                             guidInterceptor: ClientInterceptors.OutboundGUIDInterceptor?,
                                             apiKeyInterceptor: ClientInterceptors.OutboundKeyInterceptor?): OkHttpClient {
            return testClient.newBuilder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)
                    .build()
        }
    }
}

/**
 * Get a mock version of [ConnectScheduler] that will trigger the request processors to run. The
 * job processors are usually accessed in a background thread with the network requests being
 * performed synchronously but this allows us to run the processor in main thread and block the
 * tests from exiting early.
 */
internal fun getMockScheduler(): ConnectScheduler {
    val scheduler = Mockito.mock(ConnectScheduler::class.java)
    `when`(scheduler.scheduleQueuedNetworkRequests()).then {
        if (Connect.INSTANCE.isEnabled) {
            QueuedRequestsJobProcessor.process(Connect.INSTANCE.userQueue(),
                    Connect.INSTANCE.eventQueue(),
                    Connect.INSTANCE.identifyProvider(),
                    Connect.INSTANCE.eventProvider())
        }
    }
    `when`(scheduler.scheduleRecurringConfigRequests()).then {
        // Do nothing. This method triggers the same processor as single config request but repeating
    }
    `when`(scheduler.scheduleSingleConfigRequest()).then {
        ConfigJobProcessor.process(Connect.INSTANCE.configProvider(), Connect.INSTANCE.storageController())
    }
    return scheduler
}



