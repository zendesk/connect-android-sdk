package io.outbound.sdk;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;

/**
 * Created by rscott on 12/12/17.
 */

/**
 * OutboundJobScheduler schedules Firebase Jobs to be executed for tracking notifications being received and handling app uninstall tracking.
 */
public class OutboundJobScheduler {

    public final static String TAG = BuildConfig.APPLICATION_ID;

    private FirebaseJobDispatcher dispatcher;
    private Context context;

    public OutboundJobScheduler(Context context) {
        this.context = context;
        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
    }

    public void scheduleNotificationReceived(PushNotification notification) {
        String jobTag = OutboundJobService.getReceivedTag(context, notification.getInstanceId());
        scheduleJob(jobTag, notification);
    }

    public void scheduleUninstallTrack(PushNotification notification) {
        String jobTag = OutboundJobService.getUninstallTrackTag(context, notification.getInstanceId());
        scheduleJob(jobTag, notification);
    }

    private void scheduleJob(String tag, PushNotification notification) {
        Bundle notificationBundle = new Bundle();
        notificationBundle.putString(OutboundJobService.NOTIFICATION_ID_KEY, notification.getInstanceId());

        Job notificationJob = dispatcher.newJobBuilder()
                .setService(OutboundJobService.class)
                .setTag(tag)
                .setExtras(notificationBundle)
                .setRecurring(false)
                .build();

        dispatcher.mustSchedule(notificationJob);
    }

}
