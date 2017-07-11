package com.android.tph.client;

import com.android.tph.api.Logger;
import com.android.tph.api.MessageHandler;
import com.android.tph.api.PacketReceiver;
import com.android.tph.api.connection.Connection;
import com.android.tph.api.protocol.Command;
import com.android.tph.api.protocol.Packet;
import com.android.tph.handler.AckHandler;
import com.android.tph.handler.ErrorMessageHandler;
import com.android.tph.handler.FastConnectOkHandler;
import com.android.tph.handler.HandshakeOkHandler;
import com.android.tph.handler.HeartbeatHandler;
import com.android.tph.handler.KickUserHandler;
import com.android.tph.handler.OkMessageHandler;
import com.android.tph.handler.PushMessageHandler;
import com.android.tph.util.thread.ExecutorManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by kiddo on 17-7-11.
 */

public class MessageDispatcher implements PacketReceiver {
    private final Executor executor = ExecutorManager.INSTANCE.getDispatchThread();
    private final Map<Byte, MessageHandler> handlers = new HashMap<>();
    private final Logger logger = ClientConfig.I.getLogger();
    private final AckRequestMgr ackRequestMgr;

    public MessageDispatcher() {
        register(Command.HEARTBEAT, new HeartbeatHandler());
        register(Command.FAST_CONNECT, new FastConnectOkHandler());
        register(Command.HANDSHAKE, new HandshakeOkHandler());
        register(Command.KICK, new KickUserHandler());
        register(Command.OK, new OkMessageHandler());
        register(Command.ERROR, new ErrorMessageHandler());
        register(Command.PUSH, new PushMessageHandler());
        register(Command.ACK, new AckHandler());

        this.ackRequestMgr = AckRequestMgr.I();
    }

    public void register(Command command, MessageHandler handler) {
        handlers.put(command.cmd, handler);
    }

    @Override
    public void onReceive(final Packet packet, final Connection connection) {
        final MessageHandler handler = handlers.get(packet.cmd);
        if (handler != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        doAckResponse(packet);
                        handler.handle(packet, connection);
                    } catch (Throwable throwable) {
                        logger.e(throwable, "handle message error, packet=%s", packet);
                        connection.reconnect();
                    }
                }
            });
        } else {
            logger.w("<<< receive unsupported message, packet=%s", packet);
            //connection.reconnect();
        }
    }

    private void doAckResponse(Packet packet) {
        AckRequestMgr.RequestTask task = ackRequestMgr.getAndRemove(packet.sessionId);
        if (task != null) {
            task.success(packet);
        }
    }
}
