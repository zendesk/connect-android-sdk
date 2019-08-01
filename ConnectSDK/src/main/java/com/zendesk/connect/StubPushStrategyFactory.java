package com.zendesk.connect;

import javax.inject.Inject;

/**
 * Simple factory responsible for creating instances of {@link StubPushStrategy}
 */
@ConnectScope
class StubPushStrategyFactory {

    private PushStrategy stubPushStrategy;

    @Inject
    StubPushStrategyFactory() {
    }

    /**
     * Creates an instance of {@link StubPushStrategy}. Caches the created instance so subsequent
     * calls will return the same instance.
     *
     * @return an instance of {@link StubPushStrategy}
     */
    PushStrategy create() {
        if (stubPushStrategy == null) {
            stubPushStrategy = new StubPushStrategy();
        }
        return stubPushStrategy;
    }

}
