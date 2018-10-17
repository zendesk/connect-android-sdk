package com.zendesk.connect;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import retrofit2.Call;

public class ConfigProviderImpl extends BaseProvider implements ConfigProvider {

    private ConfigService service;

    public ConfigProviderImpl(OkHttpClient client, String baseUrl, Gson gson) {
        super(client, baseUrl, gson);
        service = retrofit.create(ConfigService.class);
    }

    @Override
    public Call<Config> config(String platform, String version) {
        return service.config(platform, version);
    }

}
