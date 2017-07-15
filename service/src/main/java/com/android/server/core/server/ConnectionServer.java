package com.android.server.core.server;

import com.android.server.core.ServerChannelHandler;
import com.android.server.core.connection.ConnectionManager;
import com.android.server.netty.server.NettyTCPServer;

import java.util.concurrent.ScheduledExecutorService;

import io.netty.channel.ChannelHandler;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;

/**
 * Created by kiddo on 17-7-15.
 */

public class ConnectionServer extends NettyTCPServer {
    private static ConnectionServer I;

    private ServerChannelHandler channelHandler;
    private GlobalChannelTrafficShapingHandler trafficShapingHandler;
    private ScheduledExecutorService trafficShapingExecutor;

    private ConnectionManager connectionManager = new ServerConnectionManager(true);

    public ConnectionServer(int port) {
        super(port);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return null;
    }

    @Override
    public boolean syncStart() {
        return false;
    }

    @Override
    public boolean syncStop() {
        return false;
    }
}
