package com.android.tph.api;

import com.android.tph.api.connection.Connection;
import com.android.tph.api.protocol.Packet;

/**
 * Created by kiddo on 17-7-11.
 */

public interface PacketReceiver {
    void onReceive(Packet packet, Connection connection);
}
