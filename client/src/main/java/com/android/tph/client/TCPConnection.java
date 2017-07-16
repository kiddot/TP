package com.android.tph.client;

import com.android.tph.api.Client;
import com.android.tph.api.ClientListener;
import com.android.tph.api.Logger;
import com.android.tph.api.PacketReader;
import com.android.tph.api.PacketReceiver;
import com.android.tph.api.PacketWriter;
import com.android.tph.api.connection.Connection;
import com.android.tph.api.connection.SessionContext;
import com.android.tph.api.protocol.Packet;
import com.android.tph.code.AsyncPacketReader;
import com.android.tph.code.AsyncPacketWriter;
import com.android.tph.util.IOUtils;
import com.android.tph.util.Strings;
import com.android.tph.util.thread.EventLock;

import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static com.android.tph.api.Constants.MAX_RESTART_COUNT;
import static com.android.tph.api.Constants.MAX_TOTAL_RESTART_COUNT;
import static com.android.tph.client.TCPConnection.State.connected;
import static com.android.tph.client.TCPConnection.State.connecting;
import static com.android.tph.client.TCPConnection.State.disconnected;
import static com.android.tph.client.TCPConnection.State.disconnecting;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by kiddo on 17-7-11.
 */

public final class TCPConnection implements Connection {
    public enum State {connecting, connected, disconnecting, disconnected}

    private final AtomicReference<State> state = new AtomicReference<>(disconnected);
    private final EventLock connLock = new EventLock();
    private final Logger logger;
    private final ClientListener listener;
    private final PushClient client;
    private final PacketWriter writer;
    private final PacketReader reader;
    private final AllotClient allotClient;
    private SocketChannel channel;
    private SessionContext context;
    private long lastReadTime;
    private long lastWriteTime;
    private ConnectThread connectThread;
    private int totalReconnectCount;
    private volatile int reconnectCount = 0;
    private volatile boolean autoConnect = true;

    public TCPConnection(PushClient client, PacketReceiver receiver) {
        ClientConfig config = ClientConfig.I;
        this.client = client;
        this.logger = config.getLogger();
        this.listener = config.getClientListener();
        this.allotClient = new AllotClient();
        this.reader = new AsyncPacketReader(this, receiver);
        this.writer = new AsyncPacketWriter(this, connLock);
    }

    private void onConnected(SocketChannel channel) {
        this.reconnectCount = 0;
        this.channel = channel;
        this.context = new SessionContext();
        this.state.set(connected);
        this.reader.startRead();
        logger.w("connection connected !!!");
        listener.onConnected(client);
    }

    @Override
    public void close() {
        if (state.compareAndSet(connected, disconnecting)) {
            reader.stopRead();
            if (connectThread != null) {
                connectThread.shutdown();
            }
            doClose();
            logger.w("connection closed !!!");
        }
    }

    private void doClose() {
        connLock.lock();
        try {
            Channel channel = this.channel;
            if (channel != null) {
                if (channel.isOpen()) {
                    IOUtils.close(channel);
                    listener.onDisConnected(client);
                    logger.w("channel closed !!!");
                }
                this.channel = null;
            }
        } finally {
            state.set(disconnected);
            connLock.unlock();
        }
    }

    @Override
    public void connect() {
        if (state.compareAndSet(disconnected, connecting)) {
            if ((connectThread == null) || !connectThread.isAlive()) {
                connectThread = new ConnectThread(connLock);
            }
            connectThread.addConnectTask(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return doReconnect();
                }
            });
        }
    }

    @Override
    public void reconnect() {
        close();
        connect();
    }

    private boolean doReconnect() {
        if (totalReconnectCount > MAX_TOTAL_RESTART_COUNT || !autoConnect) {// 过载保护
            logger.w("doReconnect failure reconnect count over limit or autoConnect off, total=%d, state=%s, autoConnect=%b"
                    , totalReconnectCount, state.get(), autoConnect);
            state.set(disconnected);
            return true;
        }

        reconnectCount++;    // 记录重连次数
        totalReconnectCount++;

        logger.d("try doReconnect, count=%d, total=%d, autoConnect=%b, state=%s", reconnectCount, totalReconnectCount, autoConnect, state.get());

        if (reconnectCount > MAX_RESTART_COUNT) {    // 超过此值 sleep 10min
            if (connLock.await(MINUTES.toMillis(10))) {
                state.set(disconnected);
                return true;
            }
            reconnectCount = 0;
        } else if (reconnectCount > 2) {             // 第二次重连时开始按秒sleep，然后重试
            if (connLock.await(SECONDS.toMillis(reconnectCount))) {
                state.set(disconnected);
                return true;
            }
        }

        if (Thread.currentThread().isInterrupted() || state.get() != connecting || !autoConnect) {
            logger.w("doReconnect failure, count=%d, total=%d, autoConnect=%b, state=%s", reconnectCount, totalReconnectCount, autoConnect, state.get());
            state.set(disconnected);
            return true;
        }

        logger.w("doReconnect, count=%d, total=%d, autoConnect=%b, state=%s", reconnectCount, totalReconnectCount, autoConnect, state.get());
        return doConnect();
    }

    private boolean doConnect() {
        List<String> address = allotClient.getServerAddress();
        logger.w("你好丫" + address, address);
        if (address != null && address.size() > 0) {
            for (int i = 0; i < address.size(); i++) {
                String[] host_port = address.get(i).split(":");
                logger.d("准备开始"+host_port.length);
                if (host_port.length == 2) {

                    String host = host_port[0] ;//+ ":" + host_port[1]
                    int port = Strings.toInt(host_port[1], 0);

                    logger.d("port" + port +","+ host);
                    if (doConnect(host, port)) {
                        return true;
                    }
                }
                address.remove(i--);
            }
        }
        return false;
    }

    private boolean doConnect(String host, int port) {
        connLock.lock();
        logger.w("try connect server [%s:%s]", host, port);
        SocketChannel channel = null;
        try {
            channel = SocketChannel.open();
            channel.socket().setTcpNoDelay(true);
            channel.connect(new InetSocketAddress(host, port));
            logger.w("connect server ok [%s:%s]", host, port);
            onConnected(channel);
            connLock.signalAll();
            connLock.unlock();
            return true;
        } catch (Throwable t) {
            IOUtils.close(channel);
            connLock.unlock();
            logger.e(t, "connect server ex, [%s:%s]", host, port);
        }
        return false;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.connLock.lock();
        this.autoConnect = autoConnect;
        this.connLock.signalAll();
        this.connLock.unlock();
    }

    @Override
    public void send(Packet packet) {
        writer.write(packet);
    }


    @Override
    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public boolean isConnected() {
        return state.get() == connected;
    }

    @Override
    public void setLastReadTime() {
        lastReadTime = System.currentTimeMillis();
    }

    @Override
    public void setLastWriteTime() {
        lastWriteTime = System.currentTimeMillis();
    }

    @Override
    public boolean isReadTimeout() {
        return System.currentTimeMillis() - lastReadTime > context.heartbeat + 1000;
    }

    @Override
    public void resetTimeout() {
        lastReadTime = lastWriteTime = 0;
    }

    @Override
    public boolean isAutoConnect() {
        return autoConnect;
    }

    @Override
    public SessionContext getSessionContext() {
        return context;
    }

    @Override
    public boolean isWriteTimeout() {
        return System.currentTimeMillis() - lastWriteTime > context.heartbeat - 1000;
    }

    @Override
    public String toString() {
        return "TcpConnection{" +
                "state=" + state +
                ", channel=" + channel +
                ", lastReadTime=" + lastReadTime +
                ", lastWriteTime=" + lastWriteTime +
                ", totalReconnectCount=" + totalReconnectCount +
                ", reconnectCount=" + reconnectCount +
                ", autoConnect=" + autoConnect +
                '}';
    }
}
