package com.android.tph.handler;

import com.android.tph.api.Logger;
import com.android.tph.api.connection.Connection;
import com.android.tph.api.protocol.Packet;
import com.android.tph.client.AckRequestMgr;
import com.android.tph.client.ClientConfig;
import com.android.tph.message.AckMessage;

public class AckHandler extends BaseMessageHandler<AckMessage> {

    private final Logger logger;
    private final AckRequestMgr ackRequestMgr;

    public AckHandler() {
        this.logger = ClientConfig.I.getLogger();
        this.ackRequestMgr = AckRequestMgr.I();
    }

    @Override
    public AckMessage decode(Packet packet, Connection connection) {
        return new AckMessage(packet, connection);
    }

    @Override
    public void handle(AckMessage message) {
        AckRequestMgr.RequestTask task = ackRequestMgr.getAndRemove(message.getSessionId());
        if (task != null) {
            task.success(message.getPacket());
        }
    }
}
