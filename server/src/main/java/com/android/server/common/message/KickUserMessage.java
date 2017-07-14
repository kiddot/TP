/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.android.server.common.message;

import com.android.server.api.connection.Connection;
import com.android.server.api.protocol.JsonPacket;
import com.android.server.api.protocol.Packet;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import static com.android.server.api.protocol.Command.KICK;

public class KickUserMessage extends ByteBufMessage {
    public String deviceId;
    public String userId;

    public KickUserMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    public static KickUserMessage build(Connection connection) {
        if (connection.getSessionContext().isSecurity()) {
            return new KickUserMessage(new Packet(KICK), connection);
        } else {
            return new KickUserMessage(new JsonPacket(KICK), connection);
        }
    }

    @Override
    public void decode(ByteBuf body) {
        deviceId = decodeString(body);
        userId = decodeString(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeString(body, deviceId);
        encodeString(body, userId);
    }

    @Override
    protected Map<String, String> encodeJsonBody() {
        Map<String, Object> body = new HashMap<>(2);
        body.put("deviceId", deviceId);
        body.put("userId", userId);
        return body;
    }

    @Override
    public String toString() {
        return "KickUserMessage{" +
                "deviceId='" + deviceId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
