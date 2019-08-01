package com.zendesk.connect;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import com.google.gson.Gson;

import java.util.List;

class IdentifyProviderImpl extends BaseProvider implements IdentifyProvider {

    private IdentifyService service;

    IdentifyProviderImpl(OkHttpClient client, String baseUrl, Gson gson) {
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
