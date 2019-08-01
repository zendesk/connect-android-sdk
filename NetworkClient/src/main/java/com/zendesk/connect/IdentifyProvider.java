package com.zendesk.connect;

import retrofit2.Call;

import java.util.List;

interface IdentifyProvider {

    /**
     * Identify
     * 
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> identify(User body);

    /**
     * Identify Batch
     * 
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> identifyBatch(List<User> body);

}
