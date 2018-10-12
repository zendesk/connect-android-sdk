package com.zendesk.connect;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.SimpleJobService;
import com.zendesk.logger.Logger;

/**
 * <p>
 *     {@link SimpleJobService} for scheduling the {@link ConfigJobProcessor} to fetch config and
 *     store the result the {@link BaseStorage} provided by the SDK.
 * </p>
 */
public class ConfigJobService extends SimpleJobService {

    private static final String LOG_TAG = "ConfigJobService";

    static final String CONFIG_RECURRING_JOB_TAG = "connect_config_recurring_job_tag";
    static final String CONFIG_SINGLE_JOB_TAG = "connect_config_single_job_tag";

    @Override
    public int onRunJob(JobParameters job) {
        if (!Connect.INSTANCE.isInitialised()) {
            Logger.d(LOG_TAG, "Connect has not been initialised, ending worker");
            return RESULT_FAIL_NORETRY;
        }

        Logger.d(LOG_TAG, "Starting Config config request job");

        StorageController storageController = Connect.INSTANCE.storageController();
        ConfigProvider configProvider = Connect.INSTANCE.configProvider();

        ConfigJobProcessor.process(configProvider, storageController);

        return RESULT_SUCCESS;
    }
}
