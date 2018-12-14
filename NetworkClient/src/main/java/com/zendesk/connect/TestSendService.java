package com.zendesk.connect;

import retrofit2.Call;
// This is required because there is ambiguity between okhttp3 and retrofit2
import retrofit2.http.Headers;
import retrofit2.http.*;
import okhttp3.*;
import com.zendesk.connect.PairDevice;
import java.util.*;

interface TestSendService {

    /**
     * Pair Device
     * 
     * @param platform  (required) * @param body  (optional)
     * @return Call<Void>
     */
    @Headers({"Content-Type:application/json"})
    @POST("i/testsend/push/pair/{platform}")
    Call<Void> pairDevice(@retrofit2.http.Path("platform") String platform, @retrofit2.http.Body PairDevice body);

}
