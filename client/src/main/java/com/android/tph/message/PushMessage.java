/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.android.tph.message;


import com.android.tph.api.connection.Connection;
import com.android.tph.api.protocol.Command;
import com.android.tph.api.protocol.Packet;
import com.android.tph.util.ByteBuf;

import java.nio.ByteBuffer;
import java.util.Set;

public final class PushMessage extends ByteBufMessage {

    public byte[] content;
    public String userId;
    public int clientType;
    public Set<String> tags;

    public PushMessage(byte[] content, Connection connection) {
        super(new Packet(Command.PUSH, genSessionId()), connection);
        this.content = content;
    }

    public PushMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }

    @Override
    protected void decode(ByteBuffer body) {
        content = decodeBytes(body);
        userId = decodeString(body);
    }

    @Override
    protected void encode(ByteBuf body) {
        encodeBytes(body, content);
        encodeString(body, userId);
        //encodeInt(body, clientType);
    }

    public boolean autoAck() {
        return packet.hasFlag(Packet.FLAG_AUTO_ACK);
    }

    public boolean bizAck() {
        return packet.hasFlag(Packet.FLAG_BIZ_ACK);
    }

    public PushMessage addFlag(byte flag) {
        packet.addFlag(flag);
        return this;
    }

    public PushMessage setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public PushMessage setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public PushMessage setClientType(int clientType) {
        this.clientType = clientType;
        return this;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "PushMessage{" +
                "content='" + content.length + '\'' +
                '}';
    }
}
