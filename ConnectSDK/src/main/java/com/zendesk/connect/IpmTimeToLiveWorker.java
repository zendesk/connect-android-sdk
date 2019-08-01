package com.zendesk.connect;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.zendesk.logger.Logger;

/**
 * {@link Worker} called once an {@link IpmPayload#getTimeToLive} is over.
 */
public class IpmTimeToLiveWorker extends Worker {

    private static final String LOG_TAG = "IpmTimeToLiveWorker";

    public IpmTimeToLiveWorker(@NonNull Context appContext, @NonNull WorkerParameters params) {
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

        IpmCoordinator ipmCoordinator = connectComponent.ipmCoordinator();
        ipmCoordinator.onIpmTimeToLiveEnded();

        return Result.success();
    }
}
