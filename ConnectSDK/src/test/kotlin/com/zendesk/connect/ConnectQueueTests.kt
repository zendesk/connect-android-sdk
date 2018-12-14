package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import com.squareup.tape2.ObjectQueue
import com.zendesk.logger.Logger
import org.junit.After
import org.junit.Before
import org.junit.Test

class ConnectQueueTests {

    private val NULL_OBJECT_WARNING = "Cannot add a null object to the queue"
    private val ADD_OBJECT_WARNING = "Failed to add object to queue, discarding object"
    private val RETRIEVE_OBJECT_WARNING = "Failed to retrieve object from queue"
    private val RETRIEVE_OBJECTS_WARNING = "Failed to retrieve objects from queue"
    private val REMOVE_OBJECT_WARNING = "Failed to remove objects from queue"

    private val queueGenerator = MockedObjectQueue<Any>()

    private lateinit var mockObjectQueue: ObjectQueue<Any>

    private lateinit var connectQueue: ConnectQueue<Any>

    private val logAppender = TestLogAppender().apply { Logger.addLogAppender(this) }

    @Before
    fun setup() {
        Logger.setLoggable(true)

        mockObjectQueue = queueGenerator.getObjectQueue()

        connectQueue = ConnectQueue<Any>(mockObjectQueue)
    }

    @Test
    fun `calling add should add an object to the queue`() {
        val message = "Well, Dee, this is Dennis's apartment now too"
        connectQueue.add(message)

        assertThat(mockObjectQueue.peek()).isEqualTo(message)
    }

    @Test
    fun `calling add with a null object should not add anything to the queue`() {
        connectQueue.add(null)

        assertThat(mockObjectQueue.size()).isEqualTo(0)
        assertThat(logAppender.lastLog()).isEqualTo(NULL_OBJECT_WARNING)
    }

    @Test
    fun `io exceptions caught by add should be logged`() {
        mockObjectQueue.close()
        connectQueue.add("Snap, two, three")

        assertThat(logAppender.lastLog()).isEqualTo(ADD_OBJECT_WARNING)
    }

    @Test
    fun `size should return the size of the queue`() {
        val obj1 = "I can't get this Fight Milk out of the damn couch!"
        val obj2 = "Dennis, I missed most of that last part"
        connectQueue.apply {
            add(obj1)
            add(obj2)
        }

        assertThat(connectQueue.size()).isEqualTo(2)
    }

    @Test
    fun `peek should return the first item from the queue`() {
        val obj1 = "According to the Master, guys we're not getting enough Vitamin D"
        val obj2 = "So these stickers harness the power of the sun and deliver it to our bodies"
        connectQueue.apply {
            add(obj1)
            add(obj2)
        }

        assertThat(connectQueue.peek()).isEqualTo(obj1)
    }

    @Test
    fun `peek should return null if the queue is empty`() {
        assertThat(connectQueue.peek()).isNull()
    }

    @Test
    fun `io exceptions caught by peek should be logged`() {
        mockObjectQueue.close()

        connectQueue.peek()

        assertThat(logAppender.lastLog()).isEqualTo(RETRIEVE_OBJECT_WARNING)
    }

    @Test
    fun `peek with max should return the number of items requested`() {
        val names = listOf("Charlie", "Dee", "Mac", "Dennis", "Frank")
        names.forEach { name ->
            connectQueue.add(name)
        }

        assertThat(connectQueue.peek(2)).isEqualTo(names.subList(0, 2))
    }

    @Test
    fun `peek with max should return the whole queue if the number requested is greater`() {
        val lines = listOf("It burned a hole in the cushion!", "That's probably the crowtein.")
        lines.forEach { line ->
            connectQueue.add(line)
        }

        assertThat(connectQueue.peek(100)).isEqualTo(lines)
    }

    @Test
    fun `peek with a negative max should return an empty list`() {
        val obj = "Don't get that on your skin!"
        connectQueue.add(obj)

        assertThat(connectQueue.peek(-40)).isEmpty()
    }

    @Test
    fun `io exceptions caught by peek with max should log warning`() {
        mockObjectQueue.close()
        connectQueue.peek(1)

        assertThat(logAppender.lastLog()).isEqualTo(RETRIEVE_OBJECTS_WARNING)
    }

    @Test
    fun `remove with max should remove the specified number of items from the queue`() {
        val obj1 = "Frank, sorry I'm late, man, but, uh, good news."
        val obj2 = "I brought checkers!"
        val obj3 = "I got dominoes"
        connectQueue.apply {
            add(obj1)
            add(obj2)
            add(obj3)
            remove(2)
        }

        assertThat(connectQueue.size()).isEqualTo(1)
    }

    @Test
    fun `remove with max should remove items from the front of the queue`() {
        val obj1 = "He was doing all of this annoying stuff and he was driving me crazy"
        val obj2 = "Worst of all, he was eating all of my thin mint cookies"
        connectQueue.apply {
            add(obj1)
            add(obj2)
            remove(1)
        }

        assertThat(connectQueue.peek(1)).isEqualTo(listOf(obj2))
    }

    @Test
    fun `remove with negative max should not remove anything from the queue`() {
        val obj1 = "So I created this Master character, all right?"
        val obj2 = "I typed up this newsletter"
        connectQueue.apply {
            add(obj1)
            add(obj2)
            remove(-100)
        }

        assertThat(connectQueue.size()).isEqualTo(2)
    }

    @Test
    fun `io exceptions caught by remove with max should log warning`() {
        mockObjectQueue.close()
        connectQueue.remove(1)

        assertThat(logAppender.lastLog()).isEqualTo(REMOVE_OBJECT_WARNING)
    }

    @Test
    fun `clear should remove all objects from the queue`() {
        connectQueue.apply {
            add("Hammer")
            add("Bend")
            add("Snap")
            add("Jerk")
        }

        connectQueue.clear()

        assertThat(connectQueue.size()).isEqualTo(0)
    }

    @After
    fun teardown() {
        logAppender.reset()
    }
}