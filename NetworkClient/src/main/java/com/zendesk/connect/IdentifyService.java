package com.zendesk.connect;

import java.util.List;

import retrofit2.Call;
// This is required because there is ambiguity between okhttp3 and retrofit2
import retrofit2.http.Headers;
import retrofit2.http.POST;

interface IdentifyService {

    /**
     * Identify
     * 
     * @param body  (optional)
     * @return Call<Void>
     */
    @Headers({"Content-Type:application/json"})
    @POST("v2/identify")
    Call<Void> identify(@retrofit2.http.Body User body);

    /**
     * Identify Batch
     * 
     * @param body  (optional)
     * @return Call<Void>
     */
    @Headers({"Content-Type:application/json"})
    @POST("v2/identify/batch")
    Call<Void> identifyBatch(@retrofit2.http.Body List<User> body);

}
