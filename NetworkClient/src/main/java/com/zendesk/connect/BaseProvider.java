package com.zendesk.connect;

import okhttp3.OkHttpClient;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Retrofit;
import com.google.gson.Gson;

public class BaseProvider {

    protected Retrofit retrofit;
    /**
    * Constructor BaseProvider class, internally builds a valid instance of {@link Retrofit}
    * @param client OkHttpClient instance
    * @param baseUrl String valid baseUrl to be used by the underlying {@link Retrofit} instance
    * @param gson Gson instance, configured with the marshalling strategies required for the API
    */
    public BaseProvider(OkHttpClient client, String baseUrl, Gson gson) {
        this.retrofit = new Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build();
    }

}