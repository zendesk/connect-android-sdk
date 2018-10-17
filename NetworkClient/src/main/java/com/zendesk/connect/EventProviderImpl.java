package com.zendesk.connect;

import retrofit2.Call;
import com.google.gson.Gson;
import retrofit2.http.*;
import okhttp3.*;
import com.zendesk.connect.Event;
import java.util.*;

public class EventProviderImpl extends BaseProvider implements EventProvider {

    private EventService service;

    public EventProviderImpl(OkHttpClient client, String baseUrl, Gson gson) {
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
