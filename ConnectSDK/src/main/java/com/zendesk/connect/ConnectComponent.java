package com.zendesk.connect;

import dagger.Component;

@ConnectScope
@Component(modules = {ConnectModule.class})
interface ConnectComponent {
    ConnectClient connectClient();
}
