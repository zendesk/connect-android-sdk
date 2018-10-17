package com.zendesk.connect;

import retrofit2.Call;
// This is required because there is ambiguity between okhttp3 and retrofit2
import retrofit2.http.Headers;
import retrofit2.http.*;
import okhttp3.*;
import com.zendesk.connect.Event;
import java.util.*;

public interface EventService {
    /**
    * Track
    * 
    * @param body  (optional)
    * @return Call&lt;Void&gt;
    */
    @Headers({"Content-Type:application/json"})
    @POST("v2/track")
    Call<Void> track(@retrofit2.http.Body Event body);
    /**
    * Track Batch
    * 
    * @param body  (optional)
    * @return Call&lt;Void&gt;
    */
    @Headers({"Content-Type:application/json"})
    @POST("v2/track/batch")
    Call<Void> trackBatch(@retrofit2.http.Body List<Event> body);
}
