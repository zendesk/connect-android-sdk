package com.zendesk.connect;

import com.google.gson.Gson;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;

public class IdentifyProviderImpl extends BaseProvider implements IdentifyProvider {

    private IdentifyService service;

    public IdentifyProviderImpl(OkHttpClient client, String baseUrl, Gson gson) {
        super(client, baseUrl, gson);
        service = retrofit.create(IdentifyService.class);
    }

    @Override
    public Call<Void> identify(User body) {
        return service.identify(body);
    }

    @Override
    public Call<Void> identifyBatch(List<User> body) {
        return service.identifyBatch(body);
    }

}
