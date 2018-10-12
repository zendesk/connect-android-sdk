package com.zendesk.connect;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import retrofit2.Call;

public class PushProviderImpl extends BaseProvider implements PushProvider {

    private PushService service;

    public PushProviderImpl(OkHttpClient client, String baseUrl, Gson gson) {
        super(client, baseUrl, gson);
        service = retrofit.create(PushService.class);
    }

    @Override
    public Call<Void> register(PushRegistration body) {
        return service.register(body);
    }

    @Override
    public Call<Void> unregister(PushRegistration body) {
        return service.unregister(body);
    }

}
