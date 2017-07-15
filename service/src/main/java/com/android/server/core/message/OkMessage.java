package com.android.server.core.message;

import com.android.server.core.connection.Connection;
import com.android.server.netty.codec.protocol.Packet;

import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;

import static com.android.server.netty.codec.protocol.Command.OK;

/**
 * Created by kiddo on 17-7-15.
 */

public class OkMessage extends ByteBufMessage {
    public byte cmd;
    public byte code;
    public String data;

    public OkMessage(byte cmd, Packet message, Connection connection) {
        super(message, connection);
        this.cmd = cmd;
    }

    public OkMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuf body) {
        cmd = decodeByte(body);
        code = decodeByte(body);
        data = decodeString(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeByte(body, cmd);
        encodeByte(body, code);
        encodeString(body, data);
    }

    @Override
    public Map<String, Object> encodeJsonBody() {
        Map<String, Object> body = new HashMap<>(3);
        if (cmd > 0) body.put("cmd", cmd);
        if (code > 0) body.put("code", code);
        if (data != null) body.put("data", data);
        return body;
    }

    public static OkMessage from(BaseMessage src) {
        return new OkMessage(src.packet.cmd, src.packet.response(OK), src.connection);
    }

    public OkMessage setCode(byte code) {
        this.code = code;
        return this;
    }

    public OkMessage setData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "OkMessage{" +
                "data='" + data + '\'' +
                "packet='" + packet + '\'' +
                '}';
    }
}
