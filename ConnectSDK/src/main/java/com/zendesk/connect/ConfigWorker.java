package com.zendesk.connect;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.zendesk.logger.Logger;

/**
 * {@link Worker} for scheduling the {@link ConfigJobProcessor} to fetch config and
 * store the result the {@link BaseStorage} provided by the SDK.
 */
public class ConfigWorker extends Worker {

    private static final String LOG_TAG = "ConfigWorker";

    static final String CONFIG_RECURRING_WORK_TAG = "connect_config_recurring_work_tag";

    public ConfigWorker(@NonNull Context appContext, @NonNull WorkerParameters params) {
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

        StorageController storageController = connectComponent.storageController();
        ConfigProvider configProvider = connectComponent.configProvider();

        ConfigJobProcessor.process(configProvider, storageController);

        return Result.success();
    }
}
