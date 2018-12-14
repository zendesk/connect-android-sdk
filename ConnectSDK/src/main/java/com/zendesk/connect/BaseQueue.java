package com.zendesk.connect;

import java.util.List;

interface BaseQueue<T> {

    /**
     * Adds an object to the queue
     *
     * @param object the object to be added to the queue
     */
    void add(T object);

    /**
     * Gets the current queue size
     *
     * @return the size of the queue
     */
    int size();

    /**
     * Gets the first item from the queue, or null if the queue is empty
     *
     * @return the first item from the queue, or null
     */
    T peek();

    /**
     * Gets the first n items from the queue, or all if there are less than n items
     *
     * @param max the maximum number of items to return
     * @return the list of items retrieved from the queue
     */
    List<T> peek(int max);

    /**
     * Removes n objects from the queue, or all if there are less than n items
     *
     * @param max the maximum number of items to return
     */
    void remove(int max);

    /**
     * Removes all objects from the queue
     */
    void clear();
}
