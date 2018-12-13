package com.zendesk.connect;

import retrofit2.Call;
import com.google.gson.Gson;
import retrofit2.http.*;
import okhttp3.*;
import com.zendesk.connect.PairDevice;
import java.util.*;

class TestSendProviderImpl extends BaseProvider implements TestSendProvider {

    private TestSendService service;

    TestSendProviderImpl(OkHttpClient client, String baseUrl, Gson gson) {
        super(client, baseUrl, gson);
        service = retrofit.create(TestSendService.class);
    }

    @Override
    public Call<Void> pairDevice(String platform, PairDevice body) {
        return service.pairDevice(platform, body);
    }

}
