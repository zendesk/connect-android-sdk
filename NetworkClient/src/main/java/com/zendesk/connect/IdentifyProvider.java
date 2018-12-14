package com.zendesk.connect;


import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.zendesk.connect.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface IdentifyProvider {

    /**
     * Identify
     * 
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> identify(User body);

    /**
     * Identify Batch
     * 
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> identifyBatch(List<User> body);

}
