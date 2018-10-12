package com.zendesk.connect;


import retrofit2.Call;

public interface PushProvider {
    /**
     * Register
     * 
     * @param body  (optional)
     * @return Call&lt;Void&gt;
     */
    Call<Void> register(PushRegistration body);
    /**
     * Unregister
     * 
     * @param body  (optional)
     * @return Call&lt;Void&gt;
     */
    Call<Void> unregister(PushRegistration body);

}
