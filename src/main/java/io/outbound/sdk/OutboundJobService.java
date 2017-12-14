package io.outbound.sdk;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.SimpleJobService;

import java.util.Arrays;

/**
 * Created by rscott on 12/12/17.
 */

/**
 * OutboundJobService handles scheduled Firebase jobs from the OutboundJobScheduler
 */
public class OutboundJobService extends SimpleJobService {
    public final static String TAG = BuildConfig.APPLICATION_ID;

    public final static String NOTIFICATION_ID_KEY = "notification_id";

    public final static String RECEIVED_JOB_TAG = ".outbound.job.RECEIVED";
    public final static String UNINSTALL_TRACK_JOB_TAG  = ".outbound.job.UNINSTALL_TRACK";

    public static String getReceivedTag(Context context, String notificationId) {
        return context.getPackageName() + RECEIVED_JOB_TAG + "." + notificationId;
    }

    public static String getUninstallTrackTag(Context context, String notificationId) {
        return context.getPackageName() + UNINSTALL_TRACK_JOB_TAG + "." + notificationId;
    }

    @Override
    public int onRunJob(JobParameters job) {
        String recvAction = getPackageName() + RECEIVED_JOB_TAG;
        String trackAction = getPackageName() + UNINSTALL_TRACK_JOB_TAG;

        String jobTag = job.getTag();
        Bundle jobExtras = job.getExtras();

        String instanceId = jobExtras.getString(NOTIFICATION_ID_KEY);
        if (instanceId == null) {
            Log.e(TAG, "Malformed job handled by OutboundJobService. Expected " + NOTIFICATION_ID_KEY + " to exist in the job extras. Found keys: " + Arrays.toString(jobExtras.keySet().toArray()));
            return JobService.RESULT_FAIL_NORETRY;
        }
        if (jobTag.startsWith(recvAction)) {
            OutboundClient.getInstance().receiveNotification(instanceId);
        } else if (jobTag.startsWith(trackAction)) {
            OutboundClient.getInstance().trackNotification(this, instanceId);
        } else {
            Log.e(TAG, "Unknown job tag to be handled by OutboundJobService. " + jobTag + ". Expected " + recvAction + " or " + trackAction);
        }

        return JobService.RESULT_SUCCESS;
    }
}
