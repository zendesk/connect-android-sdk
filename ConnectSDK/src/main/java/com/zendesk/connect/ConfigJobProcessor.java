package com.zendesk.connect;

import com.zendesk.logger.Logger;

import java.io.IOException;

import retrofit2.Response;

import static com.zendesk.connect.Connect.CLIENT_PLATFORM;

/**
 * Processor responsible for fetching {@link Config} objects.
 */
class ConfigJobProcessor {

    private static final String LOG_TAG = "ConfigJobProcessor";

    /**
     * Fetches a {@link Config} from Connect backend and stores the response.
     * <p>
     *     If the request doesn't return any config model then we keep the currently
     *     stored config and wait for the next scheduled config request to update again.
     * </p>
     *
     * @param configProvider an implementation of {@link ConfigProvider}
     * @param storageController an instance of {@link StorageController}
     */
    static synchronized void process(ConfigProvider configProvider, StorageController storageController) {
        if (configProvider == null || storageController == null) {
            Logger.e(LOG_TAG, "Config provider and storage controller must not be null");
            return;
        }

        try {
            Response<Config> response = configProvider
                    .config(CLIENT_PLATFORM, Connect.CLIENT_VERSION)
                    .execute();
            if (response.isSuccessful() && response.body() != null) {
                Logger.d(LOG_TAG, "Successful config retrieval. Storing response");
                storageController.saveConfig(response.body());
            } else {
                Logger.e(LOG_TAG, "Failed to retrieve config. Request returned status code:",
                        response.code());
            }
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Error while sending config request", e);
        }
    }
}
