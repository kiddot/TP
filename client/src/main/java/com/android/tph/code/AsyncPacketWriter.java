package com.android.tph.code;

import com.android.tph.api.Logger;
import com.android.tph.api.PacketWriter;
import com.android.tph.api.connection.Connection;
import com.android.tph.api.protocol.Packet;
import com.android.tph.client.ClientConfig;
import com.android.tph.util.ByteBuf;
import com.android.tph.util.thread.EventLock;
import com.android.tph.util.thread.ExecutorManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

import static com.android.tph.api.Constants.DEFAULT_WRITE_TIMEOUT;

/**
 * Created by kiddo on 17-7-11.
 */

public final class AsyncPacketWriter implements PacketWriter{
    private final Executor executor = ExecutorManager.INSTANCE.getWriteThread();
    private final Logger logger;
    private final Connection connection;
    private final EventLock connLock;
    private final ByteBuf buffer;

    public AsyncPacketWriter(Connection connection, EventLock connLock) {
        this.connection = connection;
        this.connLock = connLock;
        this.buffer = ByteBuf.allocateDirect(1024);//默认写buffer为1k
        this.logger = ClientConfig.I.getLogger();
    }

    public void write(Packet packet) {
        executor.execute(new WriteTask(packet));
    }

    private class WriteTask implements Runnable {
        private final long sendTime = System.currentTimeMillis();
        private final Packet packet;

        private WriteTask(Packet packet) {
            this.packet = packet;
        }

        @Override
        public void run() {
            buffer.clear();
            PacketEncoder.encode(packet, buffer);
            buffer.flip();
            ByteBuffer out = buffer.nioBuffer();
            logger.d("packet:" + packet);
            while (out.hasRemaining()) {
                if (connection.isConnected()) {
                    try {
                        connection.getChannel().write(out);
                        connection.setLastWriteTime();
                    } catch (IOException e) {
                        logger.e(e, "write packet ex, do reconnect, packet=%s", packet);
                        if (isTimeout()) {
                            logger.w("ignored timeout packet=%s, sendTime=%d", packet, sendTime);
                            return;
                        }
                        connection.reconnect();
                    }
                } else if (isTimeout()) {
                    logger.w("ignored timeout packet=%s, sendTime=%d", packet, sendTime);
                    return;
                } else {
                    connLock.await(DEFAULT_WRITE_TIMEOUT);
                }
            }
            logger.d("write packet end, packet=%s, costTime=%d", packet.cmd, (System.currentTimeMillis() - sendTime));
        }

        public boolean isTimeout() {
            return System.currentTimeMillis() - sendTime > DEFAULT_WRITE_TIMEOUT;
        }
    }
}
