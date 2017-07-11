package com.android.tph.message;


import com.android.tph.api.connection.Connection;
import com.android.tph.api.protocol.Command;
import com.android.tph.api.protocol.Packet;
import com.android.tph.util.ByteBuf;

import java.nio.ByteBuffer;

public class AckMessage extends ByteBufMessage {

    public AckMessage(int sessionId, Connection connection) {
        super(new Packet(Command.ACK, sessionId), connection);
    }

    public AckMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }

    @Override
    protected void decode(ByteBuffer body) {

    }

    @Override
    protected void encode(ByteBuf body) {

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