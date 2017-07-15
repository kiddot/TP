package com.android.server.core.message;

import com.android.server.core.connection.Connection;
import com.android.server.netty.codec.protocol.Packet;

import io.netty.channel.ChannelFutureListener;

/**
 * Created by kiddo on 17-7-15.
 */

public interface Message {
    Connection getConnection();

    void decodeBody();

    void encodeBody();

    /**
     * 发送当前message, 并根据情况最body进行数据压缩、加密
     *
     * @param listener 发送成功后的回调
     */
    void send(ChannelFutureListener listener);

    /**
     * 发送当前message, 不会对body进行数据压缩、加密, 原样发送
     *
     * @param listener 发送成功后的回调
     */
    void sendRaw(ChannelFutureListener listener);

    Packet getPacket();
}
