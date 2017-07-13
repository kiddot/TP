package com.android.server.core.handler;


import com.android.server.api.connection.Connection;
import com.android.server.api.protocol.Packet;
import com.android.server.common.handler.BaseMessageHandler;
import com.android.server.common.message.gateway.GatewayPushMessage;
import com.android.server.core.push.PushCenter;

public final class GatewayPushHandler extends BaseMessageHandler<GatewayPushMessage> {
    private Connection mConnection = null;

    @Override
    public GatewayPushMessage decode(Packet packet, Connection connection) {
        mConnection = connection;
        return new GatewayPushMessage(packet, connection);
    }

    @Override
    public void handle(GatewayPushMessage message) {
        PushCenter.I.push(message);
    }
}
