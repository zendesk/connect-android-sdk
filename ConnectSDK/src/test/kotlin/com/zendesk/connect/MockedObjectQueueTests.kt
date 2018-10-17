package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import com.squareup.tape2.ObjectQueue
import org.junit.Before
import org.junit.Test

/**
 * Tests for the [MockedObjectQueue] class
 */
class MockedObjectQueueTests {

    private val queueGenerator = MockedObjectQueue<Any>()

    private lateinit var mockObjectQueue: ObjectQueue<Any>

    @Before
    fun setup() {
        mockObjectQueue = queueGenerator.getObjectQueue()
    }

    @Test
    fun `add object should persist that object in the queue`() {
        val obj = "I haven't even begun to peek!"
        mockObjectQueue.add(obj)

        assertThat(queueGenerator.getCopyOfBackingList()).contains(obj)
    }

    @Test
    fun `add object should add that object to the end of the queue`() {
        val first = "The gang solves the gas crisis"
        val second = "The gang recycles their trash"
        mockObjectQueue.apply {
            add(first)
            add(second)
        }

        assertThat(mockObjectQueue.peek()).isEqualTo(first)
    }

    @Test(expected = NullPointerException::class)
    fun `add null should throw an exception`() {
        mockObjectQueue.add(null)
    }

    @Test
    fun `size should return the number of items currently in the queue`() {
        assertThat(mockObjectQueue.size()).isEqualTo(0)

        val obj = "Where do I put my feet?"
        mockObjectQueue.add(obj)

        assertThat(mockObjectQueue.size()).isEqualTo(1)
    }

    @Test
    fun `peek should return the first item from the queue`() {
        val obj1 = "I have become untethered!"
        val obj2 = "And my rage knows no bounds!"
        mockObjectQueue.apply {
            add(obj1)
            add(obj2)
        }

        assertThat(mockObjectQueue.peek()).isEqualTo(obj1)
    }

    @Test
    fun `peek should return null if the queue is empty`() {
        assertThat(mockObjectQueue.peek()).isNull()
    }

    @Test
    fun `peek with max should return that number of items from the queue`() {
        val names = listOf("Charlie", "Dee", "Mac", "Dennis", "Frank")
        names.forEach { name ->
            mockObjectQueue.add(name)
        }

        assertThat(mockObjectQueue.peek(2)).isEqualTo(names.subList(0, 2))
        assertThat(mockObjectQueue.peek(2)).hasSize(2)
    }

    @Test
    fun `peek with max should return the whole queue if the number requested is greater`() {
        val names = listOf("Charlie", "Dee", "Mac", "Dennis", "Frank")
        names.forEach { name ->
            mockObjectQueue.add(name)
        }

        assertThat(mockObjectQueue.peek(100)).isEqualTo(names)
        assertThat(mockObjectQueue.peek(100)).hasSize(names.size)
    }

    @Test
    fun `peek with max should return an empty list if the queue is empty`() {
        assertThat(mockObjectQueue.peek(100)).isEmpty()
    }

    @Test
    fun `remove should remove the first item from the queue`() {
        val obj1 = "There's a spider..."
        val obj2 = "He's deep in my soul..."
        mockObjectQueue.apply {
            add(obj1)
            add(obj2)
            remove()
        }

        assertThat(mockObjectQueue.peek()).isNotEqualTo(obj1)
        assertThat(mockObjectQueue.peek()).isEqualTo(obj2)
    }

    @Test(expected = NoSuchElementException::class)
    fun `calling remove on an empty queue should throw an exception`() {
        mockObjectQueue.remove()
    }

    @Test
    fun `calling clear should remove all items from the queue`() {
        val obj = "Look how low I can go!"
        mockObjectQueue.apply {
            add(obj)
            clear()
        }

        assertThat(mockObjectQueue.size()).isEqualTo(0)
    }

}