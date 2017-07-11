package com.android.tph.code;

import com.android.tph.api.protocol.Command;
import com.android.tph.api.protocol.Packet;
import com.android.tph.util.ByteBuf;

/**
 * Created by kiddo on 17-7-11.
 */

public class PacketEncoder {
    public static void encode(Packet packet, ByteBuf out) {

        if (packet.cmd == Command.HEARTBEAT.cmd) {
            out.put(Packet.HB_PACKET_BYTE);
        } else {
            out.putInt(packet.getBodyLength());
            out.put(packet.cmd);
            out.putShort(packet.cc);
            out.put(packet.flags);
            out.putInt(packet.sessionId);
            out.put(packet.lrc);
            if (packet.getBodyLength() > 0) {
                out.put(packet.body);
            }
        }
    }
}
