package com.android.server.common.memory;


import com.android.server.api.protocol.Command;
import com.android.server.api.protocol.Packet;
import com.android.server.api.protocol.UDPPacket;
import com.android.server.tools.config.CC;

public class PacketFactory {
    //PacketFactory FACTORY = CC.getInstance().udpGateway() ? new UDPPacket() : new Packet();

    public static Packet get(Command command) {
        if (CC.getInstance().udpGateway()){
            return new UDPPacket(command);
        }else {
            return new Packet(command);
        }
    }

}