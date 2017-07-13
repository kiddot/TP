package com.android.server.api;


import com.android.server.api.connection.Connection;
import com.android.server.api.protocol.Packet;

public interface PacketReceiver {
    void onReceive(Packet packet, Connection connection);
}
