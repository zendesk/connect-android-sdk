package com.zendesk.connect;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import com.google.gson.Gson;

import java.util.List;

class EventProviderImpl extends BaseProvider implements EventProvider {

    private EventService service;

    EventProviderImpl(OkHttpClient client, String baseUrl, Gson gson) {
        super(client, baseUrl, gson);
        service = retrofit.create(EventService.class);
    }

    @Override
    public Call<Void> track(Event body) {
        return service.track(body);
    }

    @Override
    public Call<Void> trackBatch(List<Event> body) {
        return service.trackBatch(body);
    }

}
