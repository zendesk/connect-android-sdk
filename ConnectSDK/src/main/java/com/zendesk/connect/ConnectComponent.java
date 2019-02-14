package com.zendesk.connect;

import dagger.Component;

@ConnectScope
@Component(modules = {
        ConnectModule.class,
        ConnectStorageModule.class,
        ConnectNetworkModule.class,
        ConnectNotificationModule.class})
interface ConnectComponent {

    ConnectClient client();

    StorageController storageController();

    BaseQueue<User> userQueue();

    BaseQueue<Event> eventQueue();

    ConnectScheduler scheduler();

    ConfigProvider configProvider();

    IdentifyProvider identifyProvider();

    EventProvider eventProvider();

    PushProvider pushProvider();

    MetricsProvider metricsProvider();

    TestSendProvider testSendProvider();

    ConnectInstanceId instanceId();

    NotificationProcessor notificationProcessor();

    ConnectActionProcessor actionProcessor();

    MetricRequestsProcessor metricsProcessor();

}
