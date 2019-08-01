package com.zendesk.connect;

import com.zendesk.logger.Logger;

import java.util.Map;

/**
 * Stubbed implementation of {@link PushStrategy}
 */
class StubPushStrategy implements PushStrategy {

    private static final String LOG_TAG = "StubPushStrategy";

    @Override
    public void process(Map<String, String> data) {
        Logger.d(LOG_TAG, "Stubbed push strategy called");
    }

}
