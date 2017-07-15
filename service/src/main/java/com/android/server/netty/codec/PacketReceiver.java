package com.android.server.netty.codec;

import com.android.server.core.connection.Connection;
import com.android.server.netty.codec.protocol.Packet;

/**
 * Created by kiddo on 17-7-15.
 */

public interface PacketReceiver {
    void onReceive(Packet packet, Connection connection);

}
