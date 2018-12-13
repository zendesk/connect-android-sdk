package com.zendesk.connect;


import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.zendesk.connect.PushRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface PushProvider {

    /**
     * Register
     * 
     * @param platform  (required)
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> register(String platform, PushRegistration body);

    /**
     * Unregister
     * 
     * @param platform  (required)
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> unregister(String platform, PushRegistration body);

}
