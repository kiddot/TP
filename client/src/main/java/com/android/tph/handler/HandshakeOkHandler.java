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

package com.android.tph.handler;


import com.android.tph.api.ClientListener;
import com.android.tph.api.Logger;
import com.android.tph.api.connection.Connection;
import com.android.tph.api.connection.SessionContext;
import com.android.tph.api.connection.SessionStorage;
import com.android.tph.api.protocol.Packet;
import com.android.tph.client.ClientConfig;
import com.android.tph.message.HandshakeOkMessage;
import com.android.tph.security.AesCipher;
import com.android.tph.security.CipherBox;
import com.android.tph.session.PersistentSession;

public final class HandshakeOkHandler extends BaseMessageHandler<HandshakeOkMessage> {
    private final Logger logger = ClientConfig.I.getLogger();

    @Override
    public HandshakeOkMessage decode(Packet packet, Connection connection) {
        return new HandshakeOkMessage(packet, connection);
    }

    @Override
    public void handle(HandshakeOkMessage message) {
        logger.w(">>> handshake ok message=%s", message);

        Connection connection = message.getConnection();
        SessionContext context = connection.getSessionContext();
        byte[] serverKey = message.serverKey;//在这里获取服务端返回来的随机数
        context.setHeartbeat(10000);
        if (serverKey != null){
            if (serverKey.length != CipherBox.INSTANCE.getAesKeyLength()) {
                logger.w("handshake error serverKey invalid message=%s", message);
                connection.reconnect();
                return;
            }
            //设置心跳
            context.setHeartbeat(message.heartbeat);

            //更换密钥
            AesCipher cipher = (AesCipher) context.cipher;
            byte[] sessionKey = CipherBox.INSTANCE.mixKey(cipher.key, serverKey);
            context.changeCipher(new AesCipher(sessionKey, cipher.iv));
            logger.d("更换密钥 success");
            //触发握手成功事件

        }
        if (message.heartbeat == 0) message.heartbeat = 10000 ;
        logger.d("messageheart:" + message.heartbeat);
        ClientListener listener = ClientConfig.I.getInternalListener();
        listener.onHandshakeOk(connection.getClient(), message.heartbeat);

        //保存token
        //saveToken(message, context);

    }

    private void saveToken(HandshakeOkMessage message, SessionContext context) {
        SessionStorage storage = ClientConfig.I.getSessionStorage();
        if (storage == null || message.sessionId == null) return;
        PersistentSession session = new PersistentSession();
        session.sessionId = message.sessionId;
        session.expireTime = message.expireTime;
        session.cipher = context.cipher;
        storage.saveSession(PersistentSession.encode(session));
    }
}
