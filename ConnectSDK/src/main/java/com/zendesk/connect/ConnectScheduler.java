package com.zendesk.connect;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * This class handles dispatching {@link androidx.work.Worker}s to the {@link WorkManager}
 */
@ConnectScope
class ConnectScheduler {

    private static final long CONNECT_CONFIG_REQUEST_DELAY = TimeUnit.HOURS.toMinutes(1);

    private WorkManager workManager;

    @Inject
    ConnectScheduler(WorkManager workManager) {
        this.workManager = workManager;
    }

    /**
     * Schedules a {@link PeriodicWorkRequest} to fetch and store config every hour.
     */
    void scheduleRecurringConfigRequests() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(ConfigWorker.class, CONNECT_CONFIG_REQUEST_DELAY, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        workManager.enqueueUniquePeriodicWork(
                ConfigWorker.CONFIG_RECURRING_WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                request);
    }

    /**
     * Schedules a {@link OneTimeWorkRequest} to send all the requests possible from the {@link ConnectQueue}s.
     * There should only ever be one of this job scheduled to prevent duplicate requests from
     * concurrent reading of the queues.
     */
    void scheduleQueuedNetworkRequests() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(QueuedRequestsWorker.class)
                .setConstraints(constraints)
                .build();

        workManager.enqueueUniqueWork(
                QueuedRequestsWorker.QUEUED_REQUESTS_WORKER_TAG,
                ExistingWorkPolicy.REPLACE,
                request);
    }

    /**
     * Schedules a {@link OneTimeWorkRequest} that runs once the {@param timeToLive} has passed.
     * The {@link IpmTimeToLiveWorker} is unique and is identified by the {@param workName}.
     * <p>
     * Any existing pending work with the same {@param workName} will be cancelled and replaced
     * by the new work.
     * <p>
     * A delay of 0 will be used if {@param timeToLiveInSeconds} is less than 0.
     *
     * @param workName the unique identifier for the {@link IpmTimeToLiveWorker}
     * @param timeToLiveInSeconds the initial delay before the work can start
     */
    void scheduleIpmTimeToLive(String workName, long timeToLiveInSeconds) {
        long actualDelay = Math.max(timeToLiveInSeconds, 0);
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(IpmTimeToLiveWorker.class)
                .setInitialDelay(actualDelay, TimeUnit.SECONDS)
                .build();

        workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, request);
    }

    /**
     * Cancels a previously scheduled {@link androidx.work.Worker} identified by {@param workName}.
     *
     * @param workName the unique identifier of the {@link androidx.work.Worker} to be cancelled
     */
    void cancelIpmTimeToLive(String workName) {
        workManager.cancelUniqueWork(workName);
    }

}
