package com.zendesk.connect;

import com.google.gson.Gson;

import java.util.UUID;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * Dagger module containing providers relating to networking
 */
@Module
abstract class ConnectNetworkModule {

    private static final String CONNECT_CLIENT_HEADER_FORMAT = "Android/%s";

    /**
     * Provides an instance of an {@link ClientInterceptors.OutboundClientInterceptor}
     *
     * @return an instance of an {@link ClientInterceptors.OutboundClientInterceptor}
     */
    @Provides
    @IntoSet
    @ConnectScope
    static Interceptor provideOutboundClientInterceptor() {
        String clientVersion = String.format(CONNECT_CLIENT_HEADER_FORMAT, BuildConfig.VERSION_NAME);
        return new ClientInterceptors.OutboundClientInterceptor(clientVersion);
    }

    /**
     * Provides an instance of an {@link ClientInterceptors.OutboundGuidInterceptor}
     *
     * @return an instance of an {@link ClientInterceptors.OutboundGuidInterceptor}
     */
    @Provides
    @IntoSet
    @ConnectScope
    static Interceptor provideOutboundGuidInterceptor() {
        return new ClientInterceptors.OutboundGuidInterceptor(null) {
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
    @IntoSet
    @ConnectScope
    static Interceptor provideOutboundPrivateKeyInterceptor(
            ConnectApiConfiguration connectApiConfiguration) {
        return new ClientInterceptors.OutboundKeyInterceptor(connectApiConfiguration.getApiKey());
    }

    /**
     * Provides an instance of an {@link OkHttpClient} built using {@link ConnectOkHttpClientBuilder}
     *
     * @return an instance of an {@link OkHttpClient}
     */
    @Provides
    @ConnectScope
    static OkHttpClient provideBaseOkHttpClient(ConnectOkHttpClientBuilder connectOkHttpClientBuilder) {
        return connectOkHttpClientBuilder.build();
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
    static ConfigProvider provideConfigProvider(OkHttpClient client,
                                                ConnectApiConfiguration connectApiConfiguration,
                                                Gson gson) {
        return new ConfigProviderImpl(client, connectApiConfiguration.getBaseUrl(), gson);
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
    static EventProvider provideEventProvider(OkHttpClient client,
                                              ConnectApiConfiguration connectApiConfiguration,
                                              Gson gson) {
        return new EventProviderImpl(client, connectApiConfiguration.getBaseUrl(), gson);
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
    static IdentifyProvider provideIdentifyProvider(OkHttpClient client,
                                                    ConnectApiConfiguration connectApiConfiguration,
                                                    Gson gson) {
        return new IdentifyProviderImpl(client, connectApiConfiguration.getBaseUrl(), gson);
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
    static PushProvider providePushProvider(OkHttpClient client,
                                            ConnectApiConfiguration connectApiConfiguration,
                                            Gson gson) {
        return new PushProviderImpl(client, connectApiConfiguration.getBaseUrl(), gson);
    }

    /**
     * Provides an implementation of {@link MetricsProvider}
     *
     * @param client an instance of an {@link OkHttpClient}
     * @param gson an instance of {@link Gson}
     * @return an instance of {@link MetricsProviderImpl}
     */
    @Provides
    @ConnectScope
    static MetricsProvider provideMetricsProvider(OkHttpClient client,
                                                  ConnectApiConfiguration connectApiConfiguration,
                                                  Gson gson) {
        return new MetricsProviderImpl(client, connectApiConfiguration.getBaseUrl(), gson);
    }

    @Provides
    @ConnectScope
    static TestSendProvider providerTestSendProvider(OkHttpClient client,
                                                     ConnectApiConfiguration connectApiConfiguration,
                                                     Gson gson) {
        return new TestSendProviderImpl(client, connectApiConfiguration.getBaseUrl(), gson);
    }
}
