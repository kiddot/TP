package com.android.server.core.handler;

import com.android.server.core.connection.Connection;
import com.android.server.netty.codec.protocol.Packet;

/**
 * Created by kiddo on 17-7-15.
 */

public interface MessageHandler {
    void handle(Packet packet, Connection connection);
}
