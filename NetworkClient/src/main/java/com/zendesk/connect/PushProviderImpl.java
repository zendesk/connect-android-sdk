package com.zendesk.connect;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import com.google.gson.Gson;

class PushProviderImpl extends BaseProvider implements PushProvider {

    private PushService service;

    PushProviderImpl(OkHttpClient client, String baseUrl, Gson gson) {
        super(client, baseUrl, gson);
        service = retrofit.create(PushService.class);
    }

    @Override
    public Call<Void> register(String platform, PushRegistration body) {
        return service.register(platform, body);
    }

    @Override
    public Call<Void> unregister(String platform, PushRegistration body) {
        return service.unregister(platform, body);
    }

}
