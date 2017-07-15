package com.android.server.core.message;

import com.android.server.core.connection.Connection;

/**
 * Created by kiddo on 17-7-15.
 */

public interface MessagePusher {
    void push(IPushMessage message, Connection connection);
}
