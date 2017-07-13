package com.android.server.api.spi.push;

import com.android.server.api.connection.Connection;
import com.android.server.api.spi.IPushMessage;

public interface MessagePusher {
    void push(IPushMessage message, Connection connection);
}
