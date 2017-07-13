package com.android.server.common.handler;


import com.android.server.api.MessageHandler;
import com.android.server.api.connection.Connection;
import com.android.server.api.protocol.Packet;
import com.android.server.common.message.BaseMessage;
import com.android.server.tools.common.Profiler;

public abstract class BaseMessageHandler<T extends BaseMessage> implements MessageHandler {



    public abstract T decode(Packet packet, Connection connection);

    public abstract void handle(T message);

    public void handle(Packet packet, Connection connection) {
        Profiler.enter("time cost on [message decode]");
        T t = decode(packet, connection);
        if (t != null) t.decodeBody();
        Profiler.release();

        if (t != null) {
            Profiler.enter("time cost on [handle]");
            handle(t);
            Profiler.release();
        }
    }

}
