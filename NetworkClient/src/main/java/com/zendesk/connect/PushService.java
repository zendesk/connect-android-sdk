package com.zendesk.connect;

import retrofit2.Call;
// This is required because there is ambiguity between okhttp3 and retrofit2
import retrofit2.http.Headers;
import retrofit2.http.*;

public interface PushService {
    /**
    * Register
    * 
    * @param body  (optional)
    * @return Call&lt;Void&gt;
    */
    @Headers({"Content-Type:application/json"})
    @POST("v2/gcm/register")
    Call<Void> register(@retrofit2.http.Body PushRegistration body);
    /**
    * Unregister
    * 
    * @param body  (optional)
    * @return Call&lt;Void&gt;
    */
    @Headers({"Content-Type:application/json"})
    @POST("v2/gcm/disable")
    Call<Void> unregister(@retrofit2.http.Body PushRegistration body);
}
