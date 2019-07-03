package com.zendesk.connect;

import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.zendesk.logger.Logger;

import dagger.Module;
import dagger.Provides;

@Module
public class ConnectNotificationModule {

    private static final String LOG_TAG = "ConnectNotificationModule";

    /**
     * Provides the resolved value of {@link com.zendesk.connect.R.string#connect_notification_channel_id}.
     *
     * If the integrator has not provided a notification channel id then we will provide the default
     * value defined by {@link com.zendesk.connect.R.string#_connect_notification_channel_id},
     * which may result in notifications not being displayed properly on Oreo and above.
     *
     * @param context an instance of {@link Context}
     * @return the connect notification channel id
     */
    @Provides
    @ConnectNotificationChannelQualifier
    String provideConnectNotificationChannelId(Context context) {
        String integratorChannelId = context.getString(R.string.connect_notification_channel_id);
        String defaultChannelId = context.getString(R.string._connect_notification_channel_id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && integratorChannelId.equals(defaultChannelId)) {
            Logger.w(LOG_TAG, "Notification channel id has not been provided by the " +
                    "integrator. Notifications may not be received");
        }

        return integratorChannelId;
    }

    /**
     * Provides the resolved value of {@link com.zendesk.connect.R.string#connect_silent_notification_channel_id}.
     *
     * If the integrator has not provided a silent notification channel id then we will provide
     * the same value returned by {@link #provideConnectNotificationChannelId(Context)} which may
     * result in notifications not being displayed properly on Oreo and above.
     *
     * @param context an instance of {@link Context}
     * @param connectNotificationChannelId the resolved value of
     * {@link com.zendesk.connect.R.string#connect_notification_channel_id}
     * @return the connect silent notification channel id
     */
    @Provides
    @ConnectSilentNotificationChannelQualifier
    String provideConnectSilentNotificationChannelId(
            Context context,
            @ConnectNotificationChannelQualifier String connectNotificationChannelId) {

        String integratorSilentChannelId = context.getString(R.string.connect_silent_notification_channel_id);
        String defaultChannelId = context.getString(R.string._connect_notification_channel_id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && integratorSilentChannelId.equals(defaultChannelId)) {
            Logger.w(LOG_TAG, "Silent notification channel id has not been provided by the " +
                    "integrator. Notification channel id will be used instead. " +
                    "Notifications may not be received");

            return connectNotificationChannelId;
        }

        return integratorSilentChannelId;
    }

    /**
     * Provides an instance of {@link NotificationCompat.Builder} for building {@link android.app.Notification}
     * objects. By default all notifications built by this builder will use the channel id returned
     * by {@link #provideConnectNotificationChannelId(Context)}.
     *
     * If the integrator has not provided a notification channel id then we will provide
     * a builder which may not display notifications on Oreo and above.
     *
     * @param context an instance of {@link Context}
     * @param integratorChannelId the channel id to which the notifications are sent
     * @return an instance of {@link NotificationCompat.Builder}
     */
    @Provides
    @ConnectScope
    NotificationCompat.Builder provideNotificationCompatBuilder(
            Context context,
            @ConnectNotificationChannelQualifier String integratorChannelId) {

        return new NotificationCompat.Builder(context, integratorChannelId);
    }

    /**
     * Provides an instance of {@link NotificationProcessor} for processing incoming
     * push notifications
     *
     * @param gson an instance of {@link Gson}
     * @param builder an instance of a {@link NotificationBuilder}
     * @return an instance of {@link NotificationProcessor}
     */
    @Provides
    @ConnectScope
    NotificationProcessor provideNotificationProcessor(Gson gson,
                                                       NotificationBuilder builder) {
        return new NotificationProcessor(gson, builder);
    }

    /**
     * Provides an instance of {@link ConnectActionProcessor} for processing intent
     * actions received
     *
     * @param metricsProvider an implementation of {@link MetricsProvider}
     * @return an instance of {@link ConnectActionProcessor}
     */
    @Provides
    @ConnectScope
    ConnectActionProcessor provideActionProcessor(MetricsProvider metricsProvider) {
        return new ConnectActionProcessor(metricsProvider);
    }

    /**
     * Provides an instance of {@link MetricRequestsProcessor} for processing any
     * metrics requests sent by the SDK
     *
     * @param metricsProvider an implementation of {@link MetricsProvider}
     * @return an instance of {@link MetricRequestsProcessor}
     */
    @Provides
    @ConnectScope
    MetricRequestsProcessor provideMetricsProcessor(MetricsProvider metricsProvider) {
        return new MetricRequestsProcessor(metricsProvider);
    }

}
