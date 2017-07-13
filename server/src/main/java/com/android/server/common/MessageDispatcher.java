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

package com.android.server.common;

import com.android.server.api.MessageHandler;
import com.android.server.api.PacketReceiver;
import com.android.server.api.connection.Connection;
import com.android.server.api.protocol.Command;
import com.android.server.api.protocol.Packet;
import com.android.server.common.message.ErrorMessage;
import com.android.server.tools.common.Profiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.android.server.common.ErrorCode.DISPATCH_ERROR;
import static com.android.server.common.ErrorCode.UNSUPPORTED_CMD;

public final class MessageDispatcher implements PacketReceiver {
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
            Profiler.enter("time cost on [dispatch]");
            try {
                handler.handle(packet, connection);
            } catch (Throwable throwable) {
                ErrorMessage
                        .from(packet, connection)
                        .setErrorCode(DISPATCH_ERROR)
                        .close();
            } finally {
                Profiler.release();
            }
        } else {
            if (unsupportedPolicy > POLICY_IGNORE) {
                if (unsupportedPolicy == POLICY_REJECT) {
                    ErrorMessage
                            .from(packet, connection)
                            .setErrorCode(UNSUPPORTED_CMD)
                            .close();
                }
            }
        }
    }
}