package com.zendesk.connect;

import dagger.Component;

@ConnectScope
@Component(modules = {ConnectModule.class, ConnectStorageModule.class, ConnectNetworkModule.class})
interface ConnectComponent {
    ConnectClient client();
    StorageController storageController();
    BaseQueue<String> outboundQueue();
    BaseQueue<User> userQueue();
    BaseQueue<Event> eventQueue();
    ConnectScheduler scheduler();
    ConfigProvider configProvider();
    IdentifyProvider identifyProvider();
    EventProvider eventProvider();
    PushProvider pushProvider();
    ConnectInstanceId instanceId();
}
