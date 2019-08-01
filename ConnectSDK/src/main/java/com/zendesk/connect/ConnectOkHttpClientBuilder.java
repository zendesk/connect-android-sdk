package com.zendesk.connect;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * A builder class use to build {@link OkHttpClient} with the required request headers
 * and a TLS1.2 patch
 */
@ConnectScope
class ConnectOkHttpClientBuilder {

    private final Set<Interceptor> interceptors;

    @Inject
    ConnectOkHttpClientBuilder(Set<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * Builds an instance of {@link OkHttpClient} with TLS1.2 patch, request headers for the
     * Connect API and timeout policies.
     *
     * @return an instance of {@link OkHttpClient}
     */
    OkHttpClient build() {
        OkHttpClient.Builder builder =
                Tls1Dot2SocketFactory.enableTls1Dot2OnPreLollipop(new OkHttpClient.Builder())
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS);

        for (Interceptor interceptor : interceptors) {
            builder.addInterceptor(interceptor);
        }

        return builder.build();
    }
}
