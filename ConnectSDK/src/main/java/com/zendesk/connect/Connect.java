package com.zendesk.connect;

/**
 * Connect SDK public entry point. Use this object to initialise the Connect SDK.
 */
public enum Connect {
    INSTANCE;

    private final String LOG_TAG = "ConnectSdk";

    private Client connectClient;

    /**
     * Initialise Connect SDK
     */
    public void init() {
        ConnectComponent connectComponent = DaggerConnectComponent.builder()
                .connectModule(new ConnectModule())
                .build();

        init(connectComponent);
    }

    /**
     * Internal init method for injecting {@link ConnectComponent}. Component can be swapped for
     * test dependencies if needed.
     *
     * @param component: Dagger component used to initialise the SDK
     */
    private void init(ConnectComponent component) {
        connectClient = component.connectClient();
    }

    /**
     * Provides a concrete implementation of a {@link Client} to allow
     * access to the Connect SDK functionality.
     *
     * @return a concrete implementation of {@link Client}
     */
    public Client client() {
        return connectClient;
    }
}
