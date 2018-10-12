package com.zendesk.connect;

import com.google.gson.Gson;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

/**
 * Dagger module containing providers relating to networking
 */
@Module
class ConnectNetworkModule {

    private static final String CONNECT_BASE_URL = "https://api.outbound.io";
    private static final String CONNECT_CLIENT_HEADER_FORMAT = "Android/%s";

    private final String apiKey;

    ConnectNetworkModule(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Provides an instance of an {@link ClientInterceptors.OutboundClientInterceptor}
     *
     * @return an instance of an {@link ClientInterceptors.OutboundClientInterceptor}
     */
    @Provides
    @ConnectScope
    ClientInterceptors.OutboundClientInterceptor provideOutboundClientInterceptor() {
        String clientVersion = String.format(CONNECT_CLIENT_HEADER_FORMAT, BuildConfig.VERSION_NAME);
        return new ClientInterceptors.OutboundClientInterceptor(clientVersion);
    }

    /**
     * Provides an instance of an {@link ClientInterceptors.OutboundGUIDInterceptor}
     *
     * @return an instance of an {@link ClientInterceptors.OutboundGUIDInterceptor}
     */
    @Provides
    @ConnectScope
    ClientInterceptors.OutboundGUIDInterceptor provideOutboundGuidInterceptor() {
        return new ClientInterceptors.OutboundGUIDInterceptor(null) {
            @Override
            protected String getHeaderValue() {
                return UUID.randomUUID().toString();
            }
        };
    }

    /**
     * Provides an instance of an {@link ClientInterceptors.OutboundKeyInterceptor}
     *
     * @return an instance of an {@link ClientInterceptors.OutboundKeyInterceptor
     */
    @Provides
    @ConnectScope
    ClientInterceptors.OutboundKeyInterceptor provideOutboundApiKeyInterceptor() {
        return new ClientInterceptors.OutboundKeyInterceptor(apiKey);
    }

    /**
     * Provides an instance of an {@link OkHttpClient} with the required outbound headers
     * and a TLS1.2 patch
     *
     * @param clientInterceptor the required {@link ClientInterceptors.OutboundClientInterceptor}
     * @param guidInterceptor the required {@link ClientInterceptors.OutboundGUIDInterceptor}
     * @param apiKeyInterceptor the required {@link ClientInterceptors.OutboundKeyInterceptor}
     * @return an instance of an {@link OkHttpClient}
     */
    @Provides
    @ConnectScope
    OkHttpClient provideBaseOkHttpClient(ClientInterceptors.OutboundClientInterceptor clientInterceptor,
                                         ClientInterceptors.OutboundGUIDInterceptor guidInterceptor,
                                         ClientInterceptors.OutboundKeyInterceptor apiKeyInterceptor) {
        return Tls1Dot2SocketFactory.enableTls1Dot2OnPreLollipop(new OkHttpClient.Builder())
                .addInterceptor(clientInterceptor)
                .addInterceptor(guidInterceptor)
                .addInterceptor(apiKeyInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Provides an implementation of {@link ConfigProvider}
     *
     * @param client an instance of an {@link OkHttpClient}
     * @param gson an instance of {@link Gson}
     * @return an instance of {@link ConfigProviderImpl}
     */
    @Provides
    @ConnectScope
    ConfigProvider provideConfigProvider(OkHttpClient client, Gson gson) {
        return new ConfigProviderImpl(client, CONNECT_BASE_URL, gson);
    }

    /**
     * Provides an implementation of {@link EventProvider}
     *
     * @param client an instance of an {@link OkHttpClient}
     * @param gson an instance of {@link Gson}
     * @return an instance of {@link EventProviderImpl}
     */
    @Provides
    @ConnectScope
    EventProvider provideEventProvider(OkHttpClient client, Gson gson) {
        return new EventProviderImpl(client, CONNECT_BASE_URL, gson);
    }

    /**
     * Provides an implementation of {@link IdentifyProvider}
     *
     * @param client an instance of an {@link OkHttpClient}
     * @param gson an instance of {@link Gson}
     * @return an instance of {@link IdentifyProviderImpl}
     */
    @Provides
    @ConnectScope
    IdentifyProvider provideIdentifyProvider(OkHttpClient client, Gson gson) {
        return new IdentifyProviderImpl(client, CONNECT_BASE_URL, gson);
    }

    /**
     * Provides an implementation of {@link PushProvider}
     *
     * @param client an instance of an {@link OkHttpClient}
     * @param gson an instance of {@link Gson}
     * @return an instance of {@link PushProviderImpl}
     */
    @Provides
    @ConnectScope
    PushProvider providePushProvider(OkHttpClient client, Gson gson) {
        return new PushProviderImpl(client, CONNECT_BASE_URL, gson);
    }

}
