package com.zendesk.connect;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *     This class handles dispatching {@link Job}s to the {@link FirebaseJobDispatcher}
 * </p>
 */
class ConnectScheduler {

    private FirebaseJobDispatcher dispatcher;

    ConnectScheduler(FirebaseJobDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * <p>
     *     Creates a single run {@link ConfigJobService} to immediately fetch config and store
     *     the response. We do this because the recurring job will not run immediately.
     * </p>
     */
    void scheduleSingleConfigRequest() {
        // Run a non-recurring config request job to grab a config immediately
        Job singleConfigJob = dispatcher.newJobBuilder()
                .setService(ConfigJobService.class)
                .setTag(ConfigJobService.CONFIG_SINGLE_JOB_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setReplaceCurrent(true)
                .build();

        dispatcher.mustSchedule(singleConfigJob);
    }

    /**
     * <p>
     *     Schedules a recurring {@link ConfigJobService} to fetch and store config
     *     every hour. The first time this will run will be an hour after scheduling.
     * </p>
     */
    void scheduleRecurringConfigRequests() {
        // Schedule a recurring config job to run every hour
        Job recurringConfigJob = dispatcher.newJobBuilder()
                .setService(ConfigJobService.class)
                .setTag(ConfigJobService.CONFIG_RECURRING_JOB_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setReplaceCurrent(true)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        (int) TimeUnit.SECONDS.convert(55, TimeUnit.MINUTES),
                        (int) TimeUnit.SECONDS.convert(65, TimeUnit.MINUTES)
                )).build();

        dispatcher.mustSchedule(recurringConfigJob);
    }

    /**
     * <p>
     *     Schedules a {@link Job} to send all the requests possible from the {@link ConnectQueue}s.
     *     There should only ever be one of this job scheduled to prevent duplicate requests from
     *     concurrent reading of the queues.
     * </p>
     */
    void scheduleQueuedNetworkRequests() {
        Job queuedRequestsJob = dispatcher.newJobBuilder()
                .setService(QueuedRequestsJobService.class)
                .setTag(QueuedRequestsJobService.QUEUED_REQUESTS_JOB_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setReplaceCurrent(true)
                .build();

        dispatcher.mustSchedule(queuedRequestsJob);
    }
}
