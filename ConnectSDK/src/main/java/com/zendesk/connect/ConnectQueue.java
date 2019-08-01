package com.zendesk.connect;

import com.squareup.tape2.ObjectQueue;
import com.zendesk.logger.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Controller for the object queue. Currently uses {@link ObjectQueue} but can be swapped in future
 */
class ConnectQueue<T> implements BaseQueue<T> {

    private static final String LOG_TAG = "ConnectQueue";

    private ObjectQueue<T> objectQueue;

    ConnectQueue(ObjectQueue<T> objectQueue) {
        this.objectQueue = objectQueue;
    }

    @Override
    public void add(T object) {
        if (object == null) {
            Logger.e(LOG_TAG, "Cannot add a null object to the queue");
            return;
        }

        try {
            objectQueue.add(object);
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Failed to add object to queue, discarding object", e);
        }
    }

    @Override
    public int size() {
        return objectQueue.size();
    }

    @Override
    public T peek() {
        try {
            return objectQueue.peek();
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Failed to retrieve object from queue", e);
            return null;
        }
    }

    @Override
    public List<T> peek(int max) {
        max = Math.max(max, 0); // Negative numbers become 0
        try {
            return objectQueue.peek(Math.min(size(), max));
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Failed to retrieve objects from queue", e);
            return null;
        }
    }

    @Override
    public void remove(int max) {
        max = Math.max(max, 0); // Negative numbers become 0
        try {
            objectQueue.remove(Math.min(size(), max));
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Failed to remove objects from queue", e);
        }
    }

    @Override
    public void clear() {
        try {
            objectQueue.clear();
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Failed to clear queue", e);
        }
    }
}
