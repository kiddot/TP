package com.android.server.core.handler;

import com.android.server.core.connection.Connection;
import com.android.server.core.message.BaseMessage;
import com.android.server.netty.codec.protocol.Packet;

/**
 * Created by kiddo on 17-7-15.
 */

public abstract class BaseMassageHandler <T extends BaseMessage> implements MessageHandler {

    public abstract T decode(Packet packet, Connection connection);

    public abstract void handle(T message);

    public void handle(Packet packet, Connection connection) {
        // Profiler.enter("time cost on [message decode]");
        T t = decode(packet, connection);
        if (t != null) t.decodeBody();
        // Profiler.release();

        if (t != null) {
            // Profiler.enter("time cost on [handle]");
            handle(t);
            // Profiler.release();
            }
        }
    }