package com.zendesk.connect;

import com.zendesk.logger.Logger;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;

/**
 * <p>
 *     Processor responsible for sending all Identify and Track network requests stored in the
 *     {@link BaseQueue}s.
 * </p>
 * <p>
 *     {@link QueuedRequestsJobProcessor#MAX_BATCH_SIZE} is set to 100 because that is the maximum
 *     number of items allowed by the backend for any batch request.
 * </p>
 */
class QueuedRequestsJobProcessor {

    private static final String LOG_TAG = "QueuedRequestsJobProcessor";

    private static final int MAX_BATCH_SIZE = 100;

    /**
     * Grab all objects from the {@link BaseQueue}s and sends the network requests.
     *
     * @param userQueue an instance of {@link BaseQueue} for storing {@link User} objects
     * @param eventQueue an instance of {@link BaseQueue} for storing {@link Event} objects
     * @param identifyProvider an implementation of {@link IdentifyProvider}
     * @param eventProvider an implementation of {@link EventProvider}
     */
    static synchronized void process(BaseQueue<User> userQueue,
                                     BaseQueue<Event> eventQueue,
                                     IdentifyProvider identifyProvider,
                                     EventProvider eventProvider) {

        if (userQueue == null || eventQueue == null || identifyProvider == null || eventProvider == null) {
            Logger.e(LOG_TAG, "Object queues and network providers must not be null");
            return;
        }

        Logger.d(LOG_TAG, "Beginning network request worker, sending queued items");

        try {
            processUserQueue(identifyProvider, userQueue);
            processEventQueue(eventProvider, eventQueue);
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Error while sending queued requests", e);
        }
    }

    /**
     * Processes the user queue, sending all {@link User}s in identify and identify batch requests
     * until the queue is empty.
     *
     * @param identifyProvider the provider for making identify requests
     * @param userQueue the queue containing the {@link User} objects
     */
    private static void processUserQueue(IdentifyProvider identifyProvider,
                                         BaseQueue<User> userQueue) throws IOException {
        int queueSize = userQueue.size();
        Call<Void> call = null;
        while (queueSize > 0) {
            List<User> users = userQueue.peek(MAX_BATCH_SIZE);
            if (users.size() > 1) {
                call = identifyProvider.identifyBatch(users);
            } else if (users.size() == 1) {
                call = identifyProvider.identify(users.get(0));
            }

            if (call != null && call.execute().isSuccessful()) {
                userQueue.remove(users.size());
                queueSize = userQueue.size();
            } else {
                break;
            }
        }
    }

    /**
     * Processes the event queue, sending all {@link Event}s in track and track batch requests
     * until the queue is empty.
     *
     * @param eventProvider the provider for making track requests
     * @param eventQueue the queue containing the {@link Event} objects
     */
    private static void processEventQueue(EventProvider eventProvider,
                                          BaseQueue<Event> eventQueue) throws IOException {
        int queueSize = eventQueue.size();
        Call<Void> call = null;
        while (queueSize > 0) {
            List<Event> events = eventQueue.peek(MAX_BATCH_SIZE);
            if (events.size() > 1) {
                call = eventProvider.trackBatch(events);
            } else if (events.size() == 1) {
                call = eventProvider.track(events.get(0));
            }

            if (call != null && call.execute().isSuccessful()) {
                eventQueue.remove(events.size());
                queueSize = eventQueue.size();
            } else {
                break;
            }
        }
    }
}
