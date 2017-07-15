package com.android.server.core.handler;

import com.android.server.config.ConfigManager;
import com.android.server.core.connection.Connection;
import com.android.server.core.message.FastConnectMessage;
import com.android.server.core.message.FastConnectOkMessage;
import com.android.server.netty.codec.protocol.Packet;

/**
 * Created by kiddo on 17-7-15.
 */

public class FastConnectHandler extends BaseMassageHandler<FastConnectMessage> {

    @Override
    public FastConnectMessage decode(Packet packet, Connection connection) {
        return new FastConnectMessage(packet, connection);
    }

    @Override
    public void handle(FastConnectMessage message) {
        //从缓存中心查询session
//        Profiler.enter("time cost on [query session]");
        //ReusableSession session = ReusableSessionManager.I.querySession(message.sessionId);
        //Profiler.release();
//        if (session == null) {
//            //1.没查到说明session已经失效了
//            ErrorMessage.from(message).setReason("session expired").send();
//            Logs.CONN.warn("fast connect failure, session is expired, sessionId={}, deviceId={}, conn={}"
//                    , message.sessionId, message.deviceId, message.getConnection().getChannel());
//        } else if (!session.context.deviceId.equals(message.deviceId)) {
//            //2.非法的设备, 当前设备不是上次生成session时的设备
//            ErrorMessage.from(message).setReason("invalid device").send();
//            Logs.CONN.warn("fast connect failure, not the same device, deviceId={}, session={}, conn={}"
//                    , message.deviceId, session.context, message.getConnection().getChannel());
//        } else {
            //3.校验成功，重新计算心跳，完成快速重连
            int heartbeat = ConfigManager.I.getHeartbeat(message.minHeartbeat, message.maxHeartbeat);

            //session.context.setHeartbeat(heartbeat);
            //message.getConnection().setSessionContext(session.context);
//            Profiler.enter("time cost on [send FastConnectOkMessage]");
            FastConnectOkMessage
                    .from(message)
                    .setHeartbeat(heartbeat)
                    .sendRaw();
//            Profiler.release();
//            Logs.CONN.info("fast connect success, session={}", session.context);
        //}
    }
}
