package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import com.zendesk.logger.Logger
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

@RunWith(MockitoJUnitRunner.Silent::class)
class QueuedRequestsJobProcessorTests {

    private val NULL_CONTROLLER_WARNING = "Object queues and network providers must not be null"
    private val IO_EXCEPTION_WARNING = "Error while sending queued requests"

    private val MAX_BATCH_SIZE = 100

    private lateinit var userQueue: BaseQueue<User>
    private lateinit var eventQueue: BaseQueue<Event>

    @Mock
    private lateinit var mockIdentifyProvider: IdentifyProvider

    @Mock
    private lateinit var mockEventProvider: EventProvider

    @Mock
    private lateinit var mockCall: Call<Void>

    @Mock
    private lateinit var mockResponse: Response<Void>

    private val logAppender = TestLogAppender().apply { Logger.addLogAppender(this) }

    @Before
    fun setup() {
        userQueue = ConnectQueue<User>(MockedObjectQueue<User>().getObjectQueue()).apply {
            add(UserBuilder("charlie").build())
            add(UserBuilder("mac").build())
            add(UserBuilder("dennis").build())
        }

        eventQueue = ConnectQueue<Event>(MockedObjectQueue<Event>().getObjectQueue()).apply {
            add(EventFactory.createEvent("mailroom"))
            add(EventFactory.createEvent("avatar plan"))
            add(EventFactory.createEvent("dennis system"))
        }

        Logger.setLoggable(true)

        `when`(mockIdentifyProvider.identifyBatch(any())).thenReturn(mockCall)
        `when`(mockIdentifyProvider.identify(any())).thenReturn(mockCall)
        `when`(mockEventProvider.track(any())).thenReturn(mockCall)
        `when`(mockEventProvider.trackBatch(any())).thenReturn(mockCall)

        `when`(mockCall.execute()).thenReturn(mockResponse)

        `when`(mockResponse.isSuccessful).thenReturn(true)
    }

    @After
    fun tearDown() {

    }

    @Test
    fun `null identify provider should log a warning and finish the job early`() {
        QueuedRequestsJobProcessor.process(userQueue, eventQueue, null, mockEventProvider)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_CONTROLLER_WARNING)

        verifyZeroInteractions(mockIdentifyProvider, mockEventProvider)
    }

    @Test
    fun `null event provider should log a warning and finish the job early`() {
        QueuedRequestsJobProcessor.process(userQueue, eventQueue, mockIdentifyProvider, null)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_CONTROLLER_WARNING)

        verifyZeroInteractions(mockIdentifyProvider, mockEventProvider)
    }

    @Test
    fun `null user queue should log a warning and finish the job early`() {
        QueuedRequestsJobProcessor.process(null, eventQueue, mockIdentifyProvider, mockEventProvider)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_CONTROLLER_WARNING)

        verifyZeroInteractions(mockIdentifyProvider, mockEventProvider)
    }

    @Test
    fun `null event queue should log a warning and finish the job early`() {
        QueuedRequestsJobProcessor.process(userQueue, null, mockIdentifyProvider, mockEventProvider)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_CONTROLLER_WARNING)

        verifyZeroInteractions(mockIdentifyProvider, mockEventProvider)
    }

    @Test
    fun `io exceptions thrown by network requests should caught and logged`() {
        `when`(mockCall.execute()).thenThrow(IOException())

        QueuedRequestsJobProcessor.process(userQueue, eventQueue, mockIdentifyProvider, mockEventProvider)

        assertThat(logAppender.lastLog()).contains(IO_EXCEPTION_WARNING)
    }

    @Test
    fun `an empty user queue should make no identify requests`() {
        userQueue.clear()

        QueuedRequestsJobProcessor.process(userQueue, eventQueue, mockIdentifyProvider, mockEventProvider)

        verify(mockIdentifyProvider, never()).identify(any())
        verify(mockIdentifyProvider, never()).identifyBatch(any())
    }

    @Test
    fun `a single user queued should result in an identify user request`() {
        val user = UserBuilder.anonymousUser()
        userQueue.apply {
            clear()
            add(user)
        }

        QueuedRequestsJobProcessor.process(userQueue, eventQueue, mockIdentifyProvider, mockEventProvider)

        verify(mockIdentifyProvider).identify(user)
    }

    @Test
    fun `multiple queued users should result in an identify user batch request`() {
        QueuedRequestsJobProcessor.process(userQueue, eventQueue, mockIdentifyProvider, mockEventProvider)

        verify(mockIdentifyProvider).identifyBatch(userQueue.peek(MAX_BATCH_SIZE))
    }

    @Test
    fun `a successful identify request should remove the identified users from the user queue`() {
        val initialSize = userQueue.size()
        assertThat(initialSize).isGreaterThan(0)

        QueuedRequestsJobProcessor.process(userQueue, eventQueue, mockIdentifyProvider, mockEventProvider)

        assertThat(userQueue.size()).isEqualTo(0)
    }

    @Test
    fun `an unsuccessful identify request should not remove users from the user queue`() {
        val initialSize = userQueue.size()
        assertThat(initialSize).isGreaterThan(0)

        `when`(mockResponse.isSuccessful).thenReturn(false)

        QueuedRequestsJobProcessor.process(userQueue, eventQueue, mockIdentifyProvider, mockEventProvider)

        assertThat(userQueue.size()).isEqualTo(initialSize)
    }

    @Test
    fun `an empty event queue should make no track requests`() {
        eventQueue.clear()

        QueuedRequestsJobProcessor.process(userQueue, eventQueue, mockIdentifyProvider, mockEventProvider)

        verify(mockEventProvider, never()).track(any())
        verify(mockEventProvider, never()).trackBatch(any())
    }

    @Test
    fun `a single event queued should result in a track event request`() {
        val event = EventFactory.createEvent("caw")
        eventQueue.apply {
            clear()
            add(event)
        }

        QueuedRequestsJobProcessor.process(userQueue, eventQueue, mockIdentifyProvider, mockEventProvider)

        verify(mockEventProvider).track(event)
    }

    @Test
    fun `multiple queued events should result in a track event batch request`() {
        QueuedRequestsJobProcessor.process(userQueue, eventQueue, mockIdentifyProvider, mockEventProvider)

        verify(mockEventProvider).trackBatch(eventQueue.peek(MAX_BATCH_SIZE))
    }

    @Test
    fun `a successful track request should remove the tracked events from the event queue`() {
        val initialSize = eventQueue.size()
        assertThat(initialSize).isGreaterThan(0)

        QueuedRequestsJobProcessor.process(userQueue, eventQueue, mockIdentifyProvider, mockEventProvider)

        assertThat(eventQueue.size()).isEqualTo(0)
    }

    @Test
    fun `an unsuccessful track request should not remove events from the event queue`() {
        val initialSize = eventQueue.size()
        assertThat(initialSize).isGreaterThan(0)

        `when`(mockResponse.isSuccessful).thenReturn(false)

        QueuedRequestsJobProcessor.process(userQueue, eventQueue, mockIdentifyProvider, mockEventProvider)

        assertThat(eventQueue.size()).isEqualTo(initialSize)
    }

}