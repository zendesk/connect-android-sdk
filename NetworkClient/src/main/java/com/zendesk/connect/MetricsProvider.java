package com.zendesk.connect;


import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.zendesk.connect.PushBasicMetric;
import com.zendesk.connect.UninstallTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
