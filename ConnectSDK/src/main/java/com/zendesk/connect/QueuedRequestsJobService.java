package com.zendesk.connect;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.SimpleJobService;
import com.zendesk.logger.Logger;

/**
 * <p>
 *     {@link SimpleJobService} for scheduling the {@link QueuedRequestsJobProcessor} to send all network
 *     requests in all of the queues.
 * </p>
 */
public class QueuedRequestsJobService extends SimpleJobService {

    private static final String LOG_TAG = "QueuedRequestsJobService";

    static final String QUEUED_REQUESTS_JOB_TAG = "connect_queued_requests_job_tag";

    @Override
    public int onRunJob(JobParameters job) {
        if (!Connect.INSTANCE.isEnabled()) {
            Logger.e(LOG_TAG, "Connect is not enabled, cancelling queued requests job");
            return RESULT_FAIL_NORETRY;
        }

        Logger.d(LOG_TAG, "Starting Connect queued requests job");

        BaseQueue<User> userQueue = Connect.INSTANCE.userQueue();
        BaseQueue<Event> eventQueue = Connect.INSTANCE.eventQueue();
        IdentifyProvider identifyProvider = Connect.INSTANCE.identifyProvider();
        EventProvider eventProvider = Connect.INSTANCE.eventProvider();

        QueuedRequestsJobProcessor.process(userQueue, eventQueue, identifyProvider, eventProvider);

        return RESULT_SUCCESS;
    }

}
