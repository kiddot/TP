package com.android.tph.code;

import com.android.tph.api.Logger;
import com.android.tph.api.PacketReader;
import com.android.tph.api.PacketReceiver;
import com.android.tph.api.connection.Connection;
import com.android.tph.api.protocol.Packet;
import com.android.tph.client.ClientConfig;
import com.android.tph.util.ByteBuf;
import com.android.tph.util.thread.ExecutorManager;
import com.android.tph.util.thread.NamedThreadFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by kiddo on 17-7-11.
 */

public final class AsyncPacketReader implements PacketReader, Runnable{
    private final NamedThreadFactory threadFactory = new NamedThreadFactory(ExecutorManager.READ_THREAD_NAME);
    private final Connection connection;
    private final PacketReceiver receiver;
    private final ByteBuf buffer;
    private final Logger logger;

    private Thread thread;

    public AsyncPacketReader(Connection connection, PacketReceiver receiver) {
        this.connection = connection;
        this.receiver = receiver;
        this.buffer = ByteBuf.allocateDirect(Short.MAX_VALUE);//默认读buffer大小为32k
        this.logger = ClientConfig.I.getLogger();
    }

    @Override
    public synchronized void startRead() {
        this.thread = threadFactory.newThread(this);
        this.thread.start();
    }

    @Override
    public synchronized void stopRead() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void run() {
        try {
            this.buffer.clear();
            while (connection.isConnected()) {
                ByteBuffer in = buffer.checkCapacity(1024).nioBuffer();//如果剩余空间不够每次增加1k
                if (!read(connection.getChannel(), in)) break;
                in.flip();
                decodePacket(in);
                in.compact();
            }
        } finally {
            logger.w("read an error, do reconnect!!!");
            connection.reconnect();
        }
    }

    private void decodePacket(ByteBuffer in) {
        Packet packet;
        while ((packet = PacketDecoder.decode(in)) != null) {
            //  logger.d("decode one packet=%s", packet);
            receiver.onReceive(packet, connection);
        }
    }

    private boolean read(SocketChannel channel, ByteBuffer in) {
        int readCount;
        try {
            readCount = channel.read(in);
            connection.setLastReadTime();
        } catch (IOException e) {
            logger.e(e, "read packet ex, do reconnect");
            readCount = -1;
            sleep4Reconnect();
        }
        return readCount > 0;
    }

    private void sleep4Reconnect() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
    }
}
