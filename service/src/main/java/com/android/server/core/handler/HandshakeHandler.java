package com.android.server.core.handler;

import com.android.server.config.ConfigManager;
import com.android.server.core.connection.Connection;
import com.android.server.core.connection.ReusableSession;
import com.android.server.core.connection.ReusableSessionManager;
import com.android.server.core.connection.SessionContext;
import com.android.server.core.message.BaseMessage;
import com.android.server.core.message.HandshakeMessage;
import com.android.server.core.message.HandshakeOkMessage;
import com.android.server.netty.codec.protocol.Packet;
import com.android.server.utils.Strings;
import com.android.server.utils.security.AesCipher;
import com.android.server.utils.security.CipherBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kiddo on 17-7-15.
 */

public class HandshakeHandler extends BaseMassageHandler<HandshakeMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HandshakeHandler.class);

    @Override
    public HandshakeMessage decode(Packet packet, Connection connection) {
        return new HandshakeMessage(packet, connection);
    }

    @Override
    public void handle(HandshakeMessage message) {
        if (message.getConnection().getSessionContext().isSecurity()) {
            doSecurity(message);
        } else {
            doInsecurity(message);
        }
    }

    private void doSecurity(HandshakeMessage message) {
        byte[] iv = message.iv;//AES密钥向量16位
        byte[] clientKey = message.clientKey;//客户端随机数16位
        byte[] serverKey = CipherBox.I.randomAESKey();//服务端随机数16位
        byte[] sessionKey = CipherBox.I.mixKey(clientKey, serverKey);//会话密钥16位

        //1.校验客户端消息字段
        if (Strings.isNullOrEmpty(message.deviceId)
                || iv.length != CipherBox.I.getAesKeyLength()
                || clientKey.length != CipherBox.I.getAesKeyLength()) {
            LOGGER.debug("handshake failure, message={}, conn={}", message, message.getConnection());
            return;
        }

        //2.重复握手判断
        SessionContext context = message.getConnection().getSessionContext();
        if (message.deviceId.equals(context.deviceId)) {
            LOGGER.debug("handshake failure, repeat handshake, conn={}", message.getConnection());
            return;
        }

        //3.更换会话密钥RSA=>AES(clientKey)
        context.changeCipher(new AesCipher(clientKey, iv));

        //4.生成可复用session, 用于快速重连
        ReusableSession session = ReusableSessionManager.I.genSession(context);

        //5.计算心跳时间
        int heartbeat = ConfigManager.I.getHeartbeat(message.minHeartbeat, message.maxHeartbeat);

        //6.响应握手成功消息
        HandshakeOkMessage
                .from(message)
                .setServerKey(serverKey)
                .setHeartbeat(heartbeat)
                .setSessionId(session.sessionId)
                .setExpireTime(session.expireTime)
                .send();

        //7.更换会话密钥AES(clientKey)=>AES(sessionKey)
        context.changeCipher(new AesCipher(sessionKey, iv));

        //8.保存client信息到当前连接
        context.setOsName(message.osName)
                .setOsVersion(message.osVersion)
                .setClientVersion(message.clientVersion)
                .setDeviceId(message.deviceId)
                .setHeartbeat(heartbeat);

        //9.保存可复用session到Redis, 用于快速重连
        ReusableSessionManager.I.cacheSession(session);

        LOGGER.debug("handshake success, conn={}", message.getConnection());
    }

    private void doInsecurity(HandshakeMessage message) {

        //1.校验客户端消息字段
        if (Strings.isNullOrEmpty(message.deviceId)) {
            LOGGER.debug("handshake failure, message={}, conn={}", message, message.getConnection());
            return;
        }

        //2.重复握手判断
        SessionContext context = message.getConnection().getSessionContext();
        if (message.deviceId.equals(context.deviceId)) {
            LOGGER.debug("handshake failure, repeat handshake, conn={}", message.getConnection());
            return;
        }

        //6.响应握手成功消息
        HandshakeOkMessage.from(message).send();

        //8.保存client信息到当前连接
        context.setOsName(message.osName)
                .setOsVersion(message.osVersion)
                .setClientVersion(message.clientVersion)
                .setDeviceId(message.deviceId)
                .setHeartbeat(Integer.MAX_VALUE);

        LOGGER.debug("handshake success, conn={}", message.getConnection());

    }

}
