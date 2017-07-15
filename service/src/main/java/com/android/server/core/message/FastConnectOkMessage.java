package com.android.server.core.message;

import com.android.server.core.connection.Connection;
import com.android.server.netty.codec.protocol.Packet;

import io.netty.buffer.ByteBuf;

import static com.android.server.netty.codec.protocol.Command.FAST_CONNECT;

/**
 * Created by kiddo on 17-7-15.
 */

public class FastConnectOkMessage extends ByteBufMessage {
    public int heartbeat;

    public FastConnectOkMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    public static FastConnectOkMessage from(BaseMessage src) {
        return new FastConnectOkMessage(src.packet.response(FAST_CONNECT), src.connection);
    }

    @Override
    public void decode(ByteBuf body) {
        heartbeat = decodeInt(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeInt(body, heartbeat);
    }

    public FastConnectOkMessage setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
        return this;
    }

    @Override
    public String toString() {
        return "FastConnectOkMessage{" +
                "heartbeat=" + heartbeat +
                ", packet=" + packet +
                '}';
    }
}
