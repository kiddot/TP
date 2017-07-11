package com.android.tph.api;

import com.android.tph.api.connection.Connection;
import com.android.tph.api.protocol.Packet;

/**
 * Created by kiddo on 17-7-11.
 */

public interface Message {
    Connection getConnection();

    void decodeBody();

    void encodeBody();

    void send();

    void sendRaw();

    Packet getPacket();
}
