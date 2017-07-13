package com.android.server.core.handler;


import com.android.server.api.connection.Connection;
import com.android.server.api.protocol.Packet;

public final class GatewayPushHandler extends BaseMessageHandler<GatewayPushMessage> {

    @Override
    public GatewayPushMessage decode(Packet packet, Connection connection) {
        return new GatewayPushMessage(packet, connection);
    }

    @Override
    public void handle(GatewayPushMessage message) {
        PushCenter.I.push(message);
    }
}
