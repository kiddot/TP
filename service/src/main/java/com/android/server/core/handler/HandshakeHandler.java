package com.android.server.core.handler;

import com.android.server.core.connection.Connection;
import com.android.server.core.message.BaseMessage;
import com.android.server.netty.codec.protocol.Packet;

/**
 * Created by kiddo on 17-7-15.
 */

public class HandshakeHandler extends BaseMassageHandler<HandshakeHandler> {

    @Override
    public HandshakeHandler decode(Packet packet, Connection connection) {
        return new ;
    }

    @Override
    public void handle(HandshakeHandler message) {

    }

}
