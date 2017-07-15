package com.android.server.core.message;

import com.android.server.common.Constants;
import com.android.server.core.connection.Connection;
import com.android.server.netty.codec.protocol.Packet;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Created by kiddo on 17-7-15.
 */

public abstract class ByteBufMessage extends BaseMessage {
    public ByteBufMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(byte[] body) {
        decode(Unpooled.wrappedBuffer(body));
    }

    @Override
    public byte[] encode() {
        ByteBuf body = connection.getChannel().alloc().heapBuffer();
        try {
            encode(body);
            byte[] bytes = new byte[body.readableBytes()];
            body.readBytes(bytes);
            return bytes;
        } finally {
            body.release();
        }
    }

    public abstract void decode(ByteBuf body);

    public abstract void encode(ByteBuf body);

    public void encodeString(ByteBuf body, String field) {
        try {
            encodeBytes(body, field == null ? null : field.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void encodeByte(ByteBuf body, byte field) {
        body.writeByte(field);
    }

    public void encodeInt(ByteBuf body, int field) {
        body.writeInt(field);
    }

    public void encodeLong(ByteBuf body, long field) {
        body.writeLong(field);
    }

    public void encodeBytes(ByteBuf body, byte[] field) {
        if (field == null || field.length == 0) {
            body.writeShort(0);
        } else if (field.length < Short.MAX_VALUE) {
            body.writeShort(field.length).writeBytes(field);
        } else {
            body.writeShort(Short.MAX_VALUE).writeInt(field.length - Short.MAX_VALUE).writeBytes(field);
        }
    }

    public String decodeString(ByteBuf body) {
        byte[] bytes = decodeBytes(body);
        if (bytes == null) return null;
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] decodeBytes(ByteBuf body) {
        int fieldLength = body.readShort();
        if (fieldLength == 0) return null;
        if (fieldLength == Short.MAX_VALUE) {
            fieldLength += body.readInt();
        }
        byte[] bytes = new byte[fieldLength];
        body.readBytes(bytes);
        return bytes;
    }

    public byte decodeByte(ByteBuf body) {
        return body.readByte();
    }

    public int decodeInt(ByteBuf body) {
        return body.readInt();
    }

    public long decodeLong(ByteBuf body) {
        return body.readLong();
    }
}
