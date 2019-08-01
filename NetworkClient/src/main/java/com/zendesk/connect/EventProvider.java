package com.zendesk.connect;

import retrofit2.Call;

import java.util.List;

interface EventProvider {

    /**
     * Track
     * 
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> track(Event body);

    /**
     * Track Batch
     * 
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> trackBatch(List<Event> body);

}
