package com.zendesk.connect;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.zendesk.logger.Logger;

/**
 * {@link Worker} for scheduling the {@link QueuedRequestsJobProcessor} to send all network
 * requests in all of the queues.
 */
public class QueuedRequestsWorker extends Worker {

    private static final String LOG_TAG = "QueuedRequestsWorker";

    static final String QUEUED_REQUESTS_WORKER_TAG = "connect_queued_requests_worker_tag";

    public QueuedRequestsWorker(@NonNull Context appContext, @NonNull WorkerParameters params) {
        super(appContext, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        ConnectComponent connectComponent = Connect.INSTANCE.getComponent();
        if (connectComponent == null) {
            Logger.e(LOG_TAG, "Connect has not been initialised, ending %s", LOG_TAG);
            return Result.failure();
        }

        Logger.d(LOG_TAG, "Starting %s", LOG_TAG);

        BaseQueue<User> userQueue = connectComponent.userQueue();
        BaseQueue<Event> eventQueue = connectComponent.eventQueue();
        IdentifyProvider identifyProvider = connectComponent.identifyProvider();
        EventProvider eventProvider = connectComponent.eventProvider();

        QueuedRequestsJobProcessor.process(userQueue, eventQueue, identifyProvider, eventProvider);

        return Result.success();
    }
}
