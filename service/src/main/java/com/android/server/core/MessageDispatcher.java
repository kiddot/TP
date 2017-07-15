package com.android.server.core;

import com.android.server.core.connection.Connection;
import com.android.server.core.handler.MessageHandler;
import com.android.server.netty.codec.PacketReceiver;
import com.android.server.netty.codec.protocol.Command;
import com.android.server.netty.codec.protocol.Packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kiddo on 17-7-15.
 */

public class MessageDispatcher implements PacketReceiver {
    public static final int POLICY_REJECT = 2;
    public static final int POLICY_LOG = 1;
    public static final int POLICY_IGNORE = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDispatcher.class);
    private final Map<Byte, MessageHandler> handlers = new HashMap<>();
    private final int unsupportedPolicy;

    public MessageDispatcher() {
        unsupportedPolicy = POLICY_REJECT;
    }

    public MessageDispatcher(int unsupportedPolicy) {
        this.unsupportedPolicy = unsupportedPolicy;
    }

    public void register(Command command, MessageHandler handler) {
        handlers.put(command.cmd, handler);
    }

    @Override
    public void onReceive(Packet packet, Connection connection) {
        MessageHandler handler = handlers.get(packet.cmd);
        if (handler != null) {
            //Profiler.enter("time cost on [dispatch]");
            try {
                handler.handle(packet, connection);
            } catch (Throwable throwable) {
                LOGGER.error("dispatch message ex, packet={}, connect={}, body={}"
                        , packet, connection);
//                ErrorMessage
//                        .from(packet, connection)
//                        .setErrorCode(DISPATCH_ERROR)
//                        .close();
            } finally {
                //Profiler.release();
            }
        } else {
            if (unsupportedPolicy > POLICY_IGNORE) {
                if (unsupportedPolicy == POLICY_REJECT) {
//                    ErrorMessage
//                            .from(packet, connection)
//                            .setErrorCode(UNSUPPORTED_CMD)
//                            .close();
                }
            }
        }
    }
}
