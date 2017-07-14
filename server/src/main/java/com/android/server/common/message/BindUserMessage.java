
package com.android.server.common.message;

import com.android.server.api.connection.Connection;
import com.android.server.api.protocol.Command;
import com.android.server.api.protocol.Packet;

import io.netty.buffer.ByteBuf;

import java.util.Map;

public final class BindUserMessage extends ByteBufMessage {
    public String userId;
    public String alias;
    public String tags;

    public BindUserMessage(Connection connection) {
        super(new Packet(Command.BIND, genSessionId()), connection);
    }

    public BindUserMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuf body) {
        userId = decodeString(body);
        alias = decodeString(body);
        tags = decodeString(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeString(body, userId);
        encodeString(body, alias);
        encodeString(body, tags);
    }

    @Override
    public void decodeJsonBody(Map<String, Object> body) {
        userId = (String) body.get("userId");
        tags = (String) body.get("tags");
    }

    @Override
    public String toString() {
        return "BindUserMessage{" +
                "alias='" + alias + '\'' +
                ", userId='" + userId + '\'' +
                ", tags='" + tags + '\'' +
                ", packet=" + packet +
                '}';
    }
}
