package com.zendesk.connect;


import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.zendesk.connect.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface EventProvider {

    /**
     * Track
     * 
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> track(Event body);

    /**
     * Track Batch
     * 
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> trackBatch(List<Event> body);

}
