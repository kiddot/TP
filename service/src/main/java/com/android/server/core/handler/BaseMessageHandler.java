package com.android.server.core.handler;

import com.android.server.core.connection.Connection;
import com.android.server.core.message.BaseMessage;
import com.android.server.netty.codec.protocol.Packet;

/**
 * Created by kiddo on 17-7-15.
 */

public abstract class BaseMessageHandler<T extends BaseMessage> implements MessageHandler {



    public abstract T decode(Packet packet, Connection connection);

    public abstract void handle(T message);

    public void handle(Packet packet, Connection connection) {
        T t = decode(packet, connection);
        if (t != null) t.decodeBody();

        if (t != null) {
            handle(t);
        }
    }

    /*@SuppressWarnings("unchecked")
    private final Class<T> mClass = Reflects.getSuperClassGenericType(this.getClass(), 0);

    protected T decode0(Packet packet, Connection connection) {
        if (packet.hasFlag(Packet.FLAG_JSON_STRING_BODY)) {
            String body = packet.getBody();
            T t = Jsons.fromJson(body, mClass);
            if (t != null) {
                t.setConnection(connection);
                t.setPacket(packet);
            }
            return t;
        }
        return decode(packet, connection);
    }*/
}
