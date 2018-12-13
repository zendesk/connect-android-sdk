package com.zendesk.connect;

import retrofit2.Call;
// This is required because there is ambiguity between okhttp3 and retrofit2
import retrofit2.http.Headers;
import retrofit2.http.*;
import okhttp3.*;
import com.zendesk.connect.Config;
import java.util.*;

interface ConfigService {

    /**
     * Config
     * 
     * @param platform  (required) * @param version  (required)
     * @return Call<Config>
     */
    @Headers({"Content-Type:application/json"})
    @GET("i/config/sdk/{platform}/{version}")
    Call<Config> config(@retrofit2.http.Path("platform") String platform, @retrofit2.http.Path("version") String version);

}
