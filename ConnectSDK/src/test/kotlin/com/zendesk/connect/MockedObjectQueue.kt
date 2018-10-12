package com.zendesk.connect

import com.squareup.tape2.ObjectQueue
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import java.io.IOException

/**
 * Mocked version of an [ObjectQueue] for testing. Uses a [List] as the internal data
 * structure of the queue instead of a [Queue] because [ObjectQueue] offers some methods
 * that are not immediately available from a regular queue object, and are easier to
 * mock using a list.
 */
class MockedObjectQueue<T> {

    private val list: MutableList<T> = mutableListOf()

    private var closed = false

    @Mock
    private lateinit var mockObjectQueue: ObjectQueue<T>

    /**
     * Gets a mocked [ObjectQueue]
     */
    fun getObjectQueue(): ObjectQueue<T> {
        list.clear()

        mockObjectQueue = Mockito.mock(ObjectQueue::class.java) as ObjectQueue<T>

        mockAdd()
        mockSize()
        mockPeek()
        mockPeekWithMax()
        mockRemove()
        mockRemoveWithMax()
        mockClear()
        mockClose()

        return mockObjectQueue
    }

    private fun mockAdd() {
        doAnswer {
            if (closed) throw IOException()
            val item = it.arguments[0] as T ?: throw NullPointerException()
            list.add(item)
        }.`when`(mockObjectQueue).add(any())
    }

    private fun mockSize() {
        doAnswer {
            list.size
        }.`when`(mockObjectQueue).size()
    }

    private fun mockPeek() {
        doAnswer {
            if (closed) throw IOException()
            if (list.size > 0) {
                list.first()
            } else {
                null
            }
        }.`when`(mockObjectQueue).peek()
    }

    private fun mockPeekWithMax() {
        doAnswer {
            if (closed) throw IOException()
            val max = it.arguments[0] as Int
            if (list.size > max) {
                list.subList(0, max)
            } else {
                list
            }
        }.`when`(mockObjectQueue).peek(anyInt())
    }

    private fun mockRemove() {
        doAnswer {
            if (closed) throw IOException()
            if (list.size > 0) {
                list.removeAt(0)
            } else {
                throw NoSuchElementException()
            }
        }.`when`(mockObjectQueue).remove()
    }

    private fun mockRemoveWithMax() {
        doAnswer {
            if (closed) throw IOException()
            val range = Math.min(it.arguments[0] as Int, list.size)
            when {
                list.isEmpty() -> {
                    throw NoSuchElementException()
                }
                else -> {
                    for (i in 0 until range) {
                        list.removeAt(0)
                    }
                }
            }
        }.`when`(mockObjectQueue).remove(anyInt())
    }

    private fun mockClear() {
        doAnswer {
            if (closed) throw IOException()
            list.clear()
        }.`when`(mockObjectQueue).clear()
    }

    private fun mockClose() {
        doAnswer {
            closed = true
            it
        }.`when`(mockObjectQueue).close()
    }

    /**
     * Gets an immutable copy of the list backing this mock queue for inspection
     *
     * @return an immutable copy of the list
     */
    fun getCopyOfBackingList(): List<T> {
        return list.toList()
    }
}