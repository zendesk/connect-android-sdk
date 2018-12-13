package com.zendesk.connect;

import retrofit2.Call;
// This is required because there is ambiguity between okhttp3 and retrofit2
import retrofit2.http.Headers;
import retrofit2.http.*;
import okhttp3.*;
import com.zendesk.connect.PushRegistration;
import java.util.*;

interface PushService {

    /**
     * Register
     * 
     * @param platform  (required) * @param body  (optional)
     * @return Call<Void>
     */
    @Headers({"Content-Type:application/json"})
    @POST("v2/{platform}/register")
    Call<Void> register(@retrofit2.http.Path("platform") String platform, @retrofit2.http.Body PushRegistration body);

    /**
     * Unregister
     * 
     * @param platform  (required) * @param body  (optional)
     * @return Call<Void>
     */
    @Headers({"Content-Type:application/json"})
    @POST("v2/{platform}/disable")
    Call<Void> unregister(@retrofit2.http.Path("platform") String platform, @retrofit2.http.Body PushRegistration body);

}
