package com.zendesk.connect;

import android.app.Application;

import dagger.BindsInstance;
import dagger.Component;

@ConnectScope
@Component(modules = {
        ConnectModule.class,
        ConnectStorageModule.class,
        ConnectNetworkModule.class,
        ConnectNotificationModule.class})
interface ConnectComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        @BindsInstance
        Builder connectApiConfiguration(ConnectApiConfiguration connectApiConfiguration);

        ConnectComponent build();
    }

    IpmComponent.Builder ipmComponentBuilder();

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

    IpmCoordinator ipmCoordinator();

    ForegroundListener foregroundListener();

}
