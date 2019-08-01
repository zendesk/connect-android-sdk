package com.zendesk.connect;

import retrofit2.Call;

interface MetricsProvider {

    /**
     * Opened
     * 
     * @param platform  (required)
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> opened(String platform, PushBasicMetric body);

    /**
     * Received
     * 
     * @param platform  (required)
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> received(String platform, PushBasicMetric body);

    /**
     * Uninstall Tracker
     * 
     * @param platform  (required)
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> uninstallTracker(String platform, UninstallTracker body);

}
