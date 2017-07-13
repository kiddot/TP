package com.android.server.common.memory;


import com.android.server.api.protocol.Command;
import com.android.server.api.protocol.Packet;
import com.android.server.api.protocol.UDPPacket;
import com.android.server.tools.config.CC;

public interface PacketFactory {
    PacketFactory FACTORY = CC.mp.net.udpGateway() ? UDPPacket::new : Packet::new;

    static Packet get(Command command) {
        return FACTORY.create(command);
    }

    Packet create(Command command);
}