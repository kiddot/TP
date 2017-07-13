package com.android.server.common.message;


import com.android.server.api.connection.Connection;
import com.android.server.api.protocol.Command;
import com.android.server.api.protocol.Packet;

public final class AckMessage extends BaseMessage {


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
