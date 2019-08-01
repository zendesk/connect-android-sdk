package com.zendesk.connect;

import retrofit2.Call;

interface TestSendProvider {

    /**
     * Pair Device
     * 
     * @param platform  (required)
     * @param body  (optional)
     * @return Call<Void>
     */
    Call<Void> pairDevice(String platform, PairDevice body);

}
