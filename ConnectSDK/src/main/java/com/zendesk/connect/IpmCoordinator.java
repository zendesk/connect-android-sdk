package com.zendesk.connect;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.zendesk.logger.Logger;
import com.zendesk.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Coordinator responsible for preventing more than one In-Product Message displaying on screen
 * at a time and handling processing of metrics
 */
@ConnectScope
class IpmCoordinator {

    private static final String LOG_TAG = "IpmCoordinator";

    private final IpmRepository repository;
    private final Context context;
    private final Navigator navigator;
    private final OkHttpClient okHttpClient;
    private final ForegroundListener foregroundListener;
    private final ConnectScheduler connectScheduler;
    private final NotificationProcessor notificationProcessor;
    private final IpmMetricProcessor ipmMetricProcessor;

    @Nullable
    @VisibleForTesting
    ForegroundCallback foregroundCallback;

    /**
     * Creates an instance of {@link IpmCoordinator}
     *
     * @param repository an instance of {@link IpmRepository}
     * @param context an instance of {@link Context}
     * @param navigator an instance of {@link Navigator}
     * @param okHttpClient an instance of {@link OkHttpClient}
     * @param foregroundListener an instance of {@link ForegroundListener}
     * @param connectScheduler an instance of {@link ConnectScheduler}
     */
    @Inject
    IpmCoordinator(IpmRepository repository,
                   Context context,
                   Navigator navigator,
                   OkHttpClient okHttpClient,
                   ForegroundListener foregroundListener,
                   ConnectScheduler connectScheduler,
                   NotificationProcessor notificationProcessor,
                   IpmMetricProcessor ipmMetricProcessor) {
        this.repository = repository;
        this.context = context;
        this.navigator = navigator;
        this.okHttpClient = okHttpClient;
        this.foregroundListener = foregroundListener;
        this.connectScheduler = connectScheduler;
        this.notificationProcessor = notificationProcessor;
        this.ipmMetricProcessor = ipmMetricProcessor;
    }

    /**
     * Start the given In-product message lifecycle
     * <p>
     * * If the host app is in the foreground but there's already an IPM being displayed, then it is
     * purged;
     * <p>
     * * If the host app is in the foreground and there's no IPM being displayed then it is displayed;
     * <p>
     * * If the host app is in the background then any previously received IPM will be crowded out
     * and we will start listeners to wait until this IPM can be displayed;
     *
     * @param ipmPayload the {@link IpmPayload} to be displayed
     */
    void startIpm(@NonNull IpmPayload ipmPayload) {
        String avatarUrl = ipmPayload.getLogo();
        InputStream avatarImage = null;
        if (StringUtils.hasLength(avatarUrl)) {
            avatarImage = fetchAvatarImage(avatarUrl);
        }

        if (foregroundListener.isHostAppInTheForeground()) {
            if (foregroundListener.isActivityLastResumed(IpmActivity.class)) {
                Logger.d(LOG_TAG, "IPM with oid %s has been purged",
                        ipmPayload.getInstanceId());
                return;
            }

            Logger.d(LOG_TAG, "App in the foreground, displaying IPM");

            saveIpm(ipmPayload, avatarImage);
            repository.warmUp();

            ipmMetricProcessor.trackDisplayed(ipmPayload.getInstanceId());
            navigator.startIpmActivity(context);
        } else {
            Logger.d(LOG_TAG, "App in the background, starting listeners and workers");

            IpmPayload previousIpm = repository.getIpmPayload();
            if (previousIpm != null) {
                Logger.d(LOG_TAG, "IPM with oid %s has been crowded out",
                        ipmPayload.getInstanceId());
                connectScheduler.cancelIpmTimeToLive(previousIpm.getInstanceId());
            }

            saveIpm(ipmPayload, avatarImage);

            foregroundListener.addCallback(getForegroundCallback());
            connectScheduler.scheduleIpmTimeToLive(ipmPayload.getInstanceId(),
                    ipmPayload.getTimeToLive());
        }

        if (avatarImage != null) {
            try {
                avatarImage.close();
            } catch (IOException exception) {
                Logger.d(LOG_TAG, "Avatar image InputStream failed to close");
            }
        }
    }

    /**
     * Saves the ipmPayload and the avatarImage in the {@link IpmRepository}
     *
     * @param ipmPayload the {@link IpmPayload} to be saved
     * @param avatarImage the {@link InputStream} of the avatar to be saved
     */
    @VisibleForTesting
    void saveIpm(IpmPayload ipmPayload, @Nullable InputStream avatarImage) {
        repository.setIpmPayload(ipmPayload);
        repository.setAvatarImage(avatarImage);
    }

    /**
     * Perform the action specified by the currently held {@link IpmPayload} and clear the
     * IPM from storage so a new message can be displayed
     */
    void handleIpmAction() {
        Logger.d(LOG_TAG, "handleIpmAction() called");
        ipmMetricProcessor.trackAction();
        repository.clear();
    }

    /**
     * Send any required dismiss metrics and clear the currently held IPM so a new message can
     * be displayed
     *
     * @param ipmDismissType the {@link IpmDismissType} triggered
     */
    void handleIpmDismiss(IpmDismissType ipmDismissType) {
        Logger.d(LOG_TAG,
                "handleIpmDismiss(IpmDismissType) called, IpmDismissType is: %s",
                ipmDismissType.toString());
        ipmMetricProcessor.trackDismiss();
        repository.clear();
    }

    /**
     * Send any required time to live metrics and clear the currently held IPM so a new message can
     * be displayed. If the current message has a fallback strategy that it will be applied at this
     * stage.
     */
    void onIpmTimeToLiveEnded() {
        Logger.d(LOG_TAG, "onIpmTimeToLiveEnded() called");
        foregroundListener.removeCallback(foregroundCallback);

        IpmPayload ipmPayload = repository.getIpmPayload();
        if (ipmPayload != null) {
            showAsPushNotification(ipmPayload);
        }

        repository.clear();
    }

    /**
     * Maps the given {@link IpmPayload} back to a payload that can be processed as a
     * {@link SystemPushPayload} by {@link NotificationProcessor#process}
     *
     * @param ipmPayload the {@link IpmPayload} to be processed
     */
    @VisibleForTesting
    void showAsPushNotification(IpmPayload ipmPayload) {
        Map<String, String> data = new HashMap<>();
        String instanceId = ipmPayload.getInstanceId();
        int notificationId = instanceId.hashCode();

        data.put(ConnectNotification.Keys.INSTANCE_ID.getKey(), instanceId);
        data.put(ConnectNotification.Keys.NOTIFICATION_ID.getKey(), String.valueOf(notificationId));
        data.put(ConnectNotification.Keys.TITLE.getKey(), ipmPayload.getHeading());
        data.put(ConnectNotification.Keys.BODY.getKey(), ipmPayload.getMessage());
        data.put(ConnectNotification.Keys.DEEP_LINK.getKey(), ipmPayload.getAction());

        notificationProcessor.process(data);
    }

    /**
     * Fetches the avatar image from the provided URL
     *
     * @param avatarUrl the string URL to the avatar image
     * @return the retrieved {@link InputStream}, or null
     */
    @Nullable
    @VisibleForTesting
    InputStream fetchAvatarImage(String avatarUrl) {
        InputStream avatarImage = null;
        Request request = new Request.Builder().url(avatarUrl).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                avatarImage = response.body().byteStream();
            } else {
                Logger.w(LOG_TAG, "Unable to retrieve IPM avatar image, there will be none");
            }
        } catch (IOException e) {
            Logger.w(LOG_TAG, "Unable to retrieve IPM avatar image, there will be none");
        }
        return avatarImage;
    }

    /**
     * Gets the currently processed IPM
     *
     * @return the {@link IpmPayload} currently being presented, or null if there is none
     */
    @Nullable
    IpmPayload getIpm() {
        return repository.getIpmPayload();
    }

    /**
     * Gets the avatar image for the currently processed IPM
     *
     * @return the {@link InputStream} of the avatar image, or null if there is none
     */
    @Nullable
    Bitmap getAvatarImage() {
        return repository.getAvatarImage();
    }

    /**
     * Creates an instance of {@link ForegroundCallback} to be used by the coordinator and store the
     * reference to {@link #foregroundCallback} so that the same instance can be reused if it is
     * still valid.
     *
     * @return an instance of {@link ForegroundCallback}
     */
    @VisibleForTesting
    ForegroundCallback getForegroundCallback() {
        if (foregroundCallback == null) {
            foregroundCallback = new ForegroundCallback();
        }

        return foregroundCallback;
    }

    /**
     * An implementation of {@link ForegroundListener.Callback}. When {@link #onForeground()} is
     * called this listener will remove itself from the {@link ForegroundListener} and set
     * {@link #foregroundCallback} to null; Also, if {@link IpmRepository#getIpmPayload()} is
     * not null then {@link Navigator#startIpmActivity(Context)} will be called.
     */
    @VisibleForTesting
    class ForegroundCallback implements ForegroundListener.Callback {

        /**
         * Called when the host app is foreground to display an {@link IpmPayload}.
         */
        @Override
        public void onForeground() {
            Logger.d(LOG_TAG, "onForeground() called");

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    repository.warmUp();

                    // Sanity check in case the host app was foreground before warm up did finish
                    if (foregroundListener.isHostAppInTheForeground()) {
                        foregroundListener.removeCallback(foregroundCallback);
                        foregroundCallback = null;

                        IpmPayload ipmPayload = repository.getIpmPayload();
                        if (ipmPayload != null) {
                            connectScheduler.cancelIpmTimeToLive(ipmPayload.getInstanceId());
                            ipmMetricProcessor.trackDisplayed(ipmPayload.getInstanceId());
                            navigator.startIpmActivity(context);
                        }
                    }
                }
            });
        }

    }

}
