package io.outbound.sdk;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TestInterceptor implements Interceptor {

    private HttpUrl testUrl;

    /**
     * This interceptor will redirect a request to the given url. This will be used for
     * testing the API, allowing us to give the url of a mock web server.
     *
     * @param testUrlString The url string for the redirection
     */
    TestInterceptor(String testUrlString) {
        this.testUrl = HttpUrl.parse(testUrlString);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (testUrl != null) {
            HttpUrl newUrl = request.url().newBuilder()
                    .scheme(testUrl.scheme())
                    .host(testUrl.host())
                    .port(testUrl.port())
                    .build();

            request = request.newBuilder()
                    .url(newUrl)
                    .build();
        }

        return chain.proceed(request);
    }
}
