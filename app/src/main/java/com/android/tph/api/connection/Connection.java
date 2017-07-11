package com.android.tph.api.connection;

import com.android.tph.api.Client;
import com.android.tph.api.protocol.Packet;

import java.nio.channels.SocketChannel;

/**
 * Created by kiddo on 17-7-11.
 */

public interface Connection {
    void connect();

    SessionContext getSessionContext();

    void send(Packet packet);

    void close();

    boolean isConnected();

    void reconnect();

    boolean isReadTimeout();

    boolean isWriteTimeout();

    void setLastReadTime();

    void setLastWriteTime();

    void resetTimeout();

    boolean isAutoConnect();

    SocketChannel getChannel();

    Client getClient();
}
