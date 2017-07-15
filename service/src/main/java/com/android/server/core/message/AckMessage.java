package com.android.server.core.message;

import com.android.server.core.connection.Connection;
import com.android.server.netty.codec.protocol.Command;
import com.android.server.netty.codec.protocol.Packet;

/**
 * Created by kiddo on 17-7-15.
 */

public class AckMessage extends BaseMessage {
    public AckMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }

    @Override
    public void decode(byte[] body) {

    }

    @Override
    public byte[] encode() {
        return null;
    }


    public static AckMessage from(BaseMessage src) {
        return new AckMessage(new Packet(Command.ACK, src.getSessionId()), src.connection);
    }

    @Override
    public String toString() {
        return "AckMessage{" +
                "packet=" + packet +
                '}';
    }
}
