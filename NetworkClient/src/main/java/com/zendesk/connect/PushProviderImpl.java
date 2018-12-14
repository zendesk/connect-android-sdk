package com.zendesk.connect;

import retrofit2.Call;
import com.google.gson.Gson;
import retrofit2.http.*;
import okhttp3.*;
import com.zendesk.connect.PushRegistration;
import java.util.*;

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
