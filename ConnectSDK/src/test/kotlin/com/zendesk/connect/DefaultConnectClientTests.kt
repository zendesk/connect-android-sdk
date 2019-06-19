package com.zendesk.connect

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.common.truth.Truth.assertThat
import com.google.firebase.iid.InstanceIdResult
import com.zendesk.logger.Logger
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@RunWith(MockitoJUnitRunner.Silent::class)
class DefaultConnectClientTests {

    //identify log messages
    private val NULL_USER_WARNING = "Couldn't identify a null user"
    private val IDENTIFY_USER_MESSAGE_FORMAT = "Identifying user: %s"

    //track log messages
    private val NULL_EVENT_WARNING = "Couldn't track a null event"
    private val TRACK_EVENT_MESSAGE_FORMAT = "Tracking event: %s"

    //register log messages
    private val NO_TOKEN_REGISTER_WARNING = "There is no push token to register"
    private val NULL_REGISTER_CALL = "Couldn't send register for push request"
    private val REGISTER_REQUEST_FAILED = "Failed to register for push"
    private val TOKEN_RETRIEVAL_FAILED = "Couldn't register user for push"

    //disable log messages
    private val NO_TOKEN_DISABLE_WARNING = "There is no push token to disable"
    private val NULL_DISABLE_CALL = "Couldn't send disable push request"
    private val DISABLE_REQUEST_FAILED = "Failed to disable push"

    private lateinit var connectClient: DefaultConnectClient

    private lateinit var testUser: User
    private lateinit var testStoredUser: User
    private lateinit var testEvent: Event
    private val testToken = "test_token"

    private val userCaptor = ArgumentCaptor.forClass(User::class.java)
    private val eventCaptor = ArgumentCaptor.forClass(Event::class.java)

    @Mock
    private lateinit var mockStorageController: StorageController

    @Mock
    private lateinit var mockUserQueue: BaseQueue<User>

    @Mock
    private lateinit var mockEventQueue: BaseQueue<Event>

    @Mock
    private lateinit var mockScheduler: ConnectScheduler

    @Mock
    private lateinit var mockPushProvider: PushProvider

    @Mock
    private lateinit var mockInstanceId: ConnectInstanceId

    @Mock
    private lateinit var mockIdResult: InstanceIdResult

    @Mock
    private lateinit var mockCall: Call<Void>

    @Mock
    private lateinit var mockResponse: Response<Void>

    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    @Before
    fun setUp() {
        testUser = UserBuilder("c_137")
                .setFirstName("Rick")
                .setLastName("Sanchez")
                .build()
        testStoredUser = UserBuilder.anonymousUser()
        testEvent = EventFactory.createEvent("Test Event")

        MockitoAnnotations.initMocks(DefaultConnectClientTests::class.java)

        connectClient = DefaultConnectClient(mockStorageController, mockUserQueue,
                mockEventQueue, mockScheduler, mockPushProvider, mockInstanceId)

        `when`(mockStorageController.user).thenReturn(testStoredUser)
        `when`(mockIdResult.token).thenReturn(testToken)
        `when`(mockInstanceId.getToken(any<OnSuccessListener<InstanceIdResult>>(), any<OnFailureListener>()))
                .then { (it.arguments[0] as OnSuccessListener<InstanceIdResult>).onSuccess(mockIdResult) }

        `when`(mockPushProvider.register(anyString(), any<PushRegistration>())).thenReturn(mockCall)
        `when`(mockPushProvider.unregister(anyString(), any<PushRegistration>())).thenReturn(mockCall)
        `when`(mockResponse.isSuccessful).thenReturn(true)
        `when`(mockCall.enqueue(any<Callback<Void>>())).then {
            (it.arguments[0] as Callback<Void>).onResponse(mockCall, mockResponse)
        }
    }

    @Test
    fun `calling identify with null user should log a warning and return early`() {
        connectClient.identifyUser(null)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_USER_WARNING)

        verifyZeroInteractions(mockStorageController)
        verifyZeroInteractions(mockUserQueue)
        verifyZeroInteractions(mockScheduler)
    }

    @Test
    fun `calling identify user should retrieve the active user from storage`() {
        connectClient.identifyUser(testUser)

        verify(mockStorageController).user
    }

    @Test
    fun `calling identify user with a different id to the active user should alias that user`() {
        assertThat(testUser.previousId).isNull()

        connectClient.identifyUser(testUser)

        verify(mockStorageController).saveUser(userCaptor.capture())
        assertThat(userCaptor.value.previousId).isEqualTo(testStoredUser.userId)
    }

    @Test
    fun `calling identify with a non null user should add that user to the user queue`() {
        connectClient.identifyUser(testUser)

        verify(mockUserQueue).add(userCaptor.capture())
        assertThat(userCaptor.value.userId).isEqualTo(testUser.userId)
    }

    @Test
    fun `calling identify with a non null user should add that user to storage`() {
        connectClient.identifyUser(testUser)

        verify(mockStorageController).saveUser(userCaptor.capture())
        assertThat(userCaptor.value.userId).isEqualTo(testUser.userId)
    }

    @Test
    fun `calling identify with a non null user should schedule the queued request service`() {
        connectClient.identifyUser(testUser)

        verify(mockScheduler).scheduleQueuedNetworkRequests()
    }

    @Test
    fun `calling identify with a non null user should log the id of the user being stored`() {
        connectClient.identifyUser(testUser)

        assertThat(logAppender.lastLog())
                .isEqualTo(String.format(IDENTIFY_USER_MESSAGE_FORMAT, testUser.userId))
    }

    @Test
    fun `successful token retrieval should set fcm token for the identified user`() {
        assertThat(testUser.fcm).isNull()

        connectClient.identifyUser(testUser)

        val captor = ArgumentCaptor.forClass(User::class.java)
        verify(mockStorageController).saveUser(captor.capture())
        assertThat(captor.value.fcm).contains(testToken)
    }

    @Test
    fun `failed token retrieval should not set the fcm token for the identified user`() {
        Mockito.reset(mockInstanceId)
        `when`(mockInstanceId.getToken(any<OnSuccessListener<InstanceIdResult>>(), any<OnFailureListener>()))
                .then { (it.arguments[1] as OnFailureListener).onFailure(Exception()) }

        connectClient.identifyUser(testUser)

        assertThat(testUser.fcm).isNull()
    }

    @Test
    fun `calling track with a null event should log a warning and return early`() {
        connectClient.trackEvent(null)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_EVENT_WARNING)

        verifyZeroInteractions(mockStorageController)
        verifyZeroInteractions(mockEventQueue)
        verifyZeroInteractions(mockScheduler)
    }

    @Test
    fun `calling track should retrieve the active user from storage`() {
        connectClient.trackEvent(testEvent)

        verify(mockStorageController).user
    }

    @Test
    fun `calling track should attach the active user id to the event`() {
        connectClient.trackEvent(testEvent)

        verify(mockEventQueue).add(eventCaptor.capture())
        assertThat(eventCaptor.value.userId).isEqualTo(testStoredUser.userId)
    }

    @Test
    fun `calling track should attach an anonymous user id to the event if there is no active user`() {
        Mockito.reset(mockStorageController)
        `when`(mockStorageController.user).thenReturn(null)

        assertThat(testEvent.userId).isNull()

        connectClient.trackEvent(testEvent)

        verify(mockEventQueue).add(eventCaptor.capture())
        assertThat(eventCaptor.value.userId).isNotNull()
    }

    @Test
    fun `calling track when there is no active user should result in an anonymous user being identified`() {
        Mockito.reset(mockStorageController)
        `when`(mockStorageController.user).thenReturn(null)

        connectClient.trackEvent(testEvent)

        verify(mockStorageController).saveUser(any<User>())
    }

    @Test
    fun `calling track should attach a user id to the event being tracked`() {
        assertThat(testEvent.userId).isNull()

        connectClient.trackEvent(testEvent)

        verify(mockEventQueue).add(eventCaptor.capture())
        assertThat(eventCaptor.value.userId).isNotNull()
    }

    @Test
    fun `calling track should add an event to the event queue`() {
        connectClient.trackEvent(testEvent)

        verify(mockEventQueue).add(any<Event>())
    }

    @Test
    fun `calling track should schedule the queued request service`() {
        connectClient.trackEvent(testEvent)

        verify(mockScheduler).scheduleQueuedNetworkRequests()
    }

    @Test
    fun `calling track should log the details of the event being tracked`() {
        connectClient.trackEvent(testEvent)

        verify(mockEventQueue).add(eventCaptor.capture())
        assertThat(logAppender.lastLog())
                .isEqualTo(String.format(TRACK_EVENT_MESSAGE_FORMAT, eventCaptor.value))
    }

    @Test
    fun `register for push should log a warning and return early if token retrieval fails`() {
        Mockito.reset(mockInstanceId)
        `when`(mockInstanceId.getToken(any<OnSuccessListener<InstanceIdResult>>(), any<OnFailureListener>()))
                .then { (it.arguments[1] as OnFailureListener).onFailure(Exception()) }

        connectClient.registerForPush()

        assertThat(logAppender.lastLog()).isEqualTo(TOKEN_RETRIEVAL_FAILED)
        verifyZeroInteractions(mockPushProvider)
    }

    @Test
    fun `register for push should log a warning and return early if empty token is returned`() {
        Mockito.reset(mockIdResult)
        `when`(mockIdResult.token).thenReturn("")

        connectClient.registerForPush()

        assertThat(logAppender.lastLog()).isEqualTo(NO_TOKEN_REGISTER_WARNING)
        verifyZeroInteractions(mockPushProvider)
    }

    @Test
    fun `register should identify an anonymous user if the active user is null`() {
        Mockito.reset(mockStorageController)
        `when`(mockStorageController.user).thenReturn(null)

        connectClient.registerForPush()

        verify(mockUserQueue).add(any<User>())
    }

    @Test
    fun `register for push should log a warning if push provider returns a null call object`() {
        Mockito.reset(mockPushProvider)
        `when`(mockPushProvider.register(anyString(), any<PushRegistration>())).thenReturn(null)

        connectClient.registerForPush()

        assertThat(logAppender.lastLog()).isEqualTo(NULL_REGISTER_CALL)
    }

    @Test
    fun `successful register request should set the fcm token for the active user`() {
        assertThat(testStoredUser.fcm).isNull()

        connectClient.registerForPush()

        verify(mockStorageController).saveUser(userCaptor.capture())
        assertThat(userCaptor.value.fcm).contains(testToken)
    }

    @Test
    fun `failed register request should log the failure`() {
        Mockito.reset(mockCall)
        `when`(mockCall.enqueue(any<Callback<Void>>())).then {
            (it.arguments[0] as Callback<Void>).onFailure(mockCall, Throwable())
        }

        connectClient.registerForPush()

        assertThat(logAppender.lastLog()).isEqualTo(REGISTER_REQUEST_FAILED)
    }

    @Test
    fun `disable push should log a warning and return early if the stored user has no fcm token`() {
        connectClient.disablePush()

        assertThat(logAppender.lastLog()).isEqualTo(NO_TOKEN_DISABLE_WARNING)
        verifyZeroInteractions(mockPushProvider)
    }

    @Test
    fun `disable push should log a warning if push provider returns a null call object`() {
        Mockito.reset(mockPushProvider)
        testStoredUser = UserBuilder.newBuilder(testStoredUser)
                .setFcmToken(testToken)
                .build()

        `when`(mockStorageController.user).thenReturn(testStoredUser)
        `when`(mockPushProvider.unregister(anyString(), any<PushRegistration>())).thenReturn(null)

        connectClient.disablePush()

        assertThat(logAppender.lastLog()).isEqualTo(NULL_DISABLE_CALL)
    }

    @Test
    fun `failed disable request should log a warning`() {
        testStoredUser = UserBuilder.newBuilder(testStoredUser)
                .setFcmToken(testToken)
                .build()

        `when`(mockStorageController.user).thenReturn(testStoredUser)
        `when`(mockResponse.isSuccessful).thenReturn(false)

        connectClient.disablePush()

        assertThat(logAppender.lastLog()).isEqualTo(DISABLE_REQUEST_FAILED)
    }

    @Test
    fun `logout should clear the active user from storage`() {
        connectClient.logoutUser()

        verify(mockStorageController).clearUser()
    }

    @Test
    fun `logout should clear the userQueue`() {
        connectClient.logoutUser()

        verify(mockUserQueue).clear()
    }

    @Test
    fun `logout should clear the eventQueue`() {
        connectClient.logoutUser()

        verify(mockEventQueue).clear()
    }

    @Test
    fun `logout should disable push for the active user`() {
        testStoredUser = UserBuilder.newBuilder(testStoredUser)
                .setFcmToken(testToken)
                .build()

        `when`(mockStorageController.user).thenReturn(testStoredUser)

        connectClient.logoutUser()

        verify(mockPushProvider).unregister(anyString(), any<PushRegistration>())
    }

    @Test
    fun `logout should store a new anonymous user`() {
        connectClient.logoutUser()

        val captor = ArgumentCaptor.forClass(User::class.java)

        verify(mockStorageController).saveUser(captor.capture())
        assertThat(testStoredUser).isNotEqualTo(captor.value)
    }

    @Test
    fun `logout should retrieve the fcm token for the new anonymous user`() {
        connectClient.logoutUser()

        val captor = ArgumentCaptor.forClass(User::class.java)

        verify(mockStorageController).saveUser(captor.capture())
        assertThat(captor.value.fcm).contains(testToken)
    }

    @Test
    fun `logout should store a new anonymous user even if token retrieval fails`() {
        Mockito.reset(mockInstanceId)
        `when`(mockInstanceId.getToken(any<OnSuccessListener<InstanceIdResult>>(), any<OnFailureListener>()))
                .then { (it.arguments[1] as OnFailureListener).onFailure(Exception()) }

        connectClient.logoutUser()

        verify(mockStorageController).saveUser(any<User>())
    }

    @Test
    fun `get active user should retrieve the active user from storage`() {
        assertThat(connectClient.user).isEqualTo(testStoredUser)
    }

    @After
    fun tearDown() {
        logAppender.reset()
    }
}