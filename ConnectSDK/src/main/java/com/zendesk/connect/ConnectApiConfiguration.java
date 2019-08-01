package com.zendesk.connect;

/**
 * A wrapper for the configuration required to use the Connect API.
 */
final class ConnectApiConfiguration {

    private final String baseUrl;
    private final String apiKey;

    /**
     * A wrapper for the configuration required to use the Connect API.
     *
     * @param baseUrl the connect API base url
     * @param apiKey a Connect private API key
     */
    ConnectApiConfiguration(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    String getBaseUrl() {
        return baseUrl;
    }

    String getApiKey() {
        return apiKey;
    }
}
