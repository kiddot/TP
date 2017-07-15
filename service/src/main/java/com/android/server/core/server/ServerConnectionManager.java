package com.android.server.core.server;

import com.android.server.config.ConfigCenter;
import com.android.server.core.ServerChannelHandler;
import com.android.server.core.connection.Connection;
import com.android.server.core.connection.ConnectionManager;
import com.android.server.netty.connection.NettyConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * Created by kiddo on 17-7-15.
 */

public class ServerConnectionManager implements ConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConnectionManager.class);
    private final ConcurrentMap<ChannelId, ConnectionHolder> connections = new ConcurrentHashMap<>();
    private final ConnectionHolder DEFAULT = new SimpleConnectionHolder(null);
    private final boolean heartbeatCheck;
    private final ConnectionHolderFactory holderFactory;
    private HashedWheelTimer timer;

    public ServerConnectionManager(boolean heartbeatCheck) {
        this.heartbeatCheck = heartbeatCheck;
        this.holderFactory = heartbeatCheck ? new ConnectionHolderFactory() {
            @Override
            public ConnectionHolder create(Connection connection) {
                return new HeartbeatCheckTask(connection);
            }
        } : new ConnectionHolderFactory() {
            @Override
            public ConnectionHolder create(Connection connection) {
                return new SimpleConnectionHolder(connection);
            }
        };
    }

    @Override
    public Connection get(Channel channel) {
        if (!connections.containsKey(channel.id())){
            return DEFAULT.get();
        }
        return connections.get(channel.id()).get();
    }

    @Override
    public Connection removeAndClose(Channel channel) {
        ConnectionHolder holder = connections.remove(channel.id());
        if (holder != null) {
            Connection connection = holder.get();
            holder.close();
            return connection;
        }

        //add default
        Connection connection = new NettyConnection();
        connection.init(channel, false);
        connection.close();
        return connection;
    }

    @Override
    public void add(final Connection connection) {
        if (connections.containsKey(connection.getChannel().id())){
            return;
        }else {
            connections.put(connection.getChannel().id(), new ConnectionHolder() {
                @Override
                public Connection get() {
                    return connection;
                }

                @Override
                public void close() {
                    connection.close();
                }
            });
        }
    }

    @Override
    public int getConnNum() {
        return connections.size();
    }

    @Override
    public void init() {
        if (heartbeatCheck) {
            long tickDuration = TimeUnit.SECONDS.toMillis(1);//1s 每秒钟走一步，一个心跳周期内大致走一圈
            int ticksPerWheel = (int) (ConfigCenter.max_heartbeat / tickDuration);
            this.timer = new HashedWheelTimer(
                    tickDuration, TimeUnit.MILLISECONDS, ticksPerWheel
            );
        }
    }

    @Override
    public void destroy() {
        if (timer != null) {
            timer.stop();
        }
        //connections.values().forEach(ConnectionHolder::close);
        Iterator it = connections.values().iterator();

        while (it.hasNext()) {
            ConnectionHolder connectionHolder = (ConnectionHolder) it.next();
            connectionHolder.close();
        }

        connections.clear();
    }

    private interface ConnectionHolder {
        Connection get();

        void close();
    }

    private static class SimpleConnectionHolder implements ConnectionHolder {
        private final Connection connection;

        private SimpleConnectionHolder(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Connection get() {
            return connection;
        }

        @Override
        public void close() {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private class HeartbeatCheckTask implements ConnectionHolder, TimerTask {

        private byte timeoutTimes = 0;
        private Connection connection;

        private HeartbeatCheckTask(Connection connection) {
            this.connection = connection;
            this.startTimeout();
        }

        void startTimeout() {
            Connection connection = this.connection;

            if (connection != null && connection.isConnected()) {
                int timeout = connection.getSessionContext().heartbeat;
                timer.newTimeout(this, timeout, TimeUnit.MILLISECONDS);
            }
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            Connection connection = this.connection;

            if (connection == null || !connection.isConnected()) {
                LOGGER.debug("heartbeat timeout times={}, connection disconnected, conn={}", timeoutTimes, connection);
                return;
            }

            if (connection.isReadTimeout()) {
                if (++timeoutTimes > ConfigCenter.max_hb_timeout_times) {
                    connection.close();
                    LOGGER.debug("client heartbeat timeout times={}, do close conn={}", timeoutTimes, connection);
                    return;
                } else {
                    LOGGER.debug("client heartbeat timeout times={}, connection={}", timeoutTimes, connection);
                }
            } else {
                timeoutTimes = 0;
            }
            startTimeout();
        }

        @Override
        public void close() {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        }

        @Override
        public Connection get() {
            return connection;
        }
    }

    private interface ConnectionHolderFactory {
        ConnectionHolder create(Connection connection);
    }
}
