package com.zendesk.connect;

import retrofit2.Call;
// This is required because there is ambiguity between okhttp3 and retrofit2
import retrofit2.http.Headers;
import retrofit2.http.POST;

interface MetricsService {

    /**
     * Opened
     * 
     * @param platform  (required) * @param body  (optional)
     * @return Call<Void>
     */
    @Headers({"Content-Type:application/json"})
    @POST("i/{platform}/opened")
    Call<Void> opened(@retrofit2.http.Path("platform") String platform,
                      @retrofit2.http.Body PushBasicMetric body);

    /**
     * Received
     * 
     * @param platform  (required) * @param body  (optional)
     * @return Call<Void>
     */
    @Headers({"Content-Type:application/json"})
    @POST("i/{platform}/received")
    Call<Void> received(@retrofit2.http.Path("platform") String platform,
                        @retrofit2.http.Body PushBasicMetric body);

    /**
     * Uninstall Tracker
     * 
     * @param platform  (required) * @param body  (optional)
     * @return Call<Void>
     */
    @Headers({"Content-Type:application/json"})
    @POST("i/{platform}/uninstall_tracker")
    Call<Void> uninstallTracker(@retrofit2.http.Path("platform") String platform,
                                @retrofit2.http.Body UninstallTracker body);

}
