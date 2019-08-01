package com.zendesk.connect;

import retrofit2.Call;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.zendesk.connect.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface ConfigProvider {

    /**
     * Config
     * 
     * @param platform  (required)
     * @param version  (required)
     * @return Call<Config>
     */
    Call<Config> config(String platform, String version);

}
