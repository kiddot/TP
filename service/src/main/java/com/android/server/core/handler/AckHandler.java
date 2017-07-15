package com.android.server.core.handler;

import com.android.server.core.connection.Connection;
import com.android.server.core.message.AckMessage;
import com.android.server.core.task.AckTask;
import com.android.server.core.task.AckTaskQueue;
import com.android.server.netty.codec.protocol.Packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kiddo on 17-7-15.
 */

public class AckHandler extends BaseMassageHandler<AckMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AckHandler.class);


    @Override
    public AckMessage decode(Packet packet, Connection connection) {
        return new AckMessage(packet, connection);
    }

    @Override
    public void handle(AckMessage message) {
        AckTask task = AckTaskQueue.I.getAndRemove(message.getSessionId());
        if (task == null) {//ack 超时了
            LOGGER.debug("receive client ack, but task timeout message={}", message);
            return;
        }

        task.onResponse();//成功收到客户的ACK响应
    }
}
