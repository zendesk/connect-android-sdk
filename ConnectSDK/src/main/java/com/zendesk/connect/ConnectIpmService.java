package com.zendesk.connect;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.zendesk.logger.Logger;

/**
 * Service class used for processing In-Product Messages. Work is performed in this service context
 * instead of the push receiving service because of the imposed time restrictions.
 */
public class ConnectIpmService extends JobIntentService {

    private static final String LOG_TAG = "ConnectIpmService";
    private static final int JOB_ID = 1;

    static final String IPM_PAYLOAD_PARCELABLE_KEY = "IPM_PAYLOAD_PARCELABLE_KEY";

    public static void enqueueWork(@NonNull Context context, @NonNull Intent work) {
        JobIntentService.enqueueWork(context, ConnectIpmService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        ConnectComponent connectComponent = Connect.INSTANCE.getComponent();
        if (connectComponent == null) {
            Logger.e(LOG_TAG, "Connect was not initialised, ending IPM job service");
            return;
        }

        if (!intent.hasExtra(IPM_PAYLOAD_PARCELABLE_KEY)) {
            Logger.e(LOG_TAG, "Unable to retrieve extra from Intent, ending IPM job service");
            return;
        }

        IpmPayload ipmPayload = intent.getParcelableExtra(IPM_PAYLOAD_PARCELABLE_KEY);

        if (ipmPayload == null) {
            Logger.e(LOG_TAG, "IPM payload was null, ending IPM job service");
            return;
        }

        IpmCoordinator coordinator = connectComponent.ipmCoordinator();
        coordinator.startIpm(ipmPayload);
    }

}
