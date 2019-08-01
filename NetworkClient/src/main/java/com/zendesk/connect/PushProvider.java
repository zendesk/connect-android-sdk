package com.zendesk.connect;

import retrofit2.Call;

interface PushProvider {

    /**
     * Register
     * 
     * @param platform  (required)
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> register(String platform, PushRegistration body);

    /**
     * Unregister
     * 
     * @param platform  (required)
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> unregister(String platform, PushRegistration body);

}
