package com.zendesk.connect;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import com.google.gson.Gson;

class MetricsProviderImpl extends BaseProvider implements MetricsProvider {

    private MetricsService service;

    MetricsProviderImpl(OkHttpClient client, String baseUrl, Gson gson) {
        super(client, baseUrl, gson);
        service = retrofit.create(MetricsService.class);
    }

    @Override
    public Call<Void> opened(String platform, PushBasicMetric body) {
        return service.opened(platform, body);
    }

    @Override
    public Call<Void> received(String platform, PushBasicMetric body) {
        return service.received(platform, body);
    }

    @Override
    public Call<Void> uninstallTracker(String platform, UninstallTracker body) {
        return service.uninstallTracker(platform, body);
    }

}
