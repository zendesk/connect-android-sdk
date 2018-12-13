package com.zendesk.connect;

import retrofit2.Call;
import com.google.gson.Gson;
import retrofit2.http.*;
import okhttp3.*;
import com.zendesk.connect.Config;
import java.util.*;

class ConfigProviderImpl extends BaseProvider implements ConfigProvider {

    private ConfigService service;

    ConfigProviderImpl(OkHttpClient client, String baseUrl, Gson gson) {
        super(client, baseUrl, gson);
        service = retrofit.create(ConfigService.class);
    }

    @Override
    public Call<Config> config(String platform, String version) {
        return service.config(platform, version);
    }

}
