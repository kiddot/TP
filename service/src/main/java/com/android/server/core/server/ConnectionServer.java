package com.android.server.core.server;

import com.android.server.config.ConfigCenter;
import com.android.server.core.MessageDispatcher;
import com.android.server.core.ServerChannelHandler;
import com.android.server.core.connection.Connection;
import com.android.server.core.connection.ConnectionManager;
import com.android.server.core.handler.AckHandler;
import com.android.server.core.handler.BindUserHandler;
import com.android.server.core.handler.ClientPushHandler;
import com.android.server.core.handler.FastConnectHandler;
import com.android.server.core.handler.HandshakeHandler;
import com.android.server.core.handler.HeartBeatHandler;
import com.android.server.core.handler.MessageHandler;
import com.android.server.netty.codec.protocol.Command;
import com.android.server.netty.codec.protocol.Packet;
import com.android.server.netty.server.NettyTCPServer;

import java.util.concurrent.Executors;
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


    public static ConnectionServer I() {
        if (I == null) {
            synchronized (ConnectionServer.class) {
                if (I == null) {
                    I = new ConnectionServer(ConfigCenter.connect_server_port);
                }
            }
        }
        return I;
    }

    private ConnectionServer(int port) {
        super(port);
    }

    @Override
    public void init() {
        super.init();
        connectionManager.init();
        MessageDispatcher receiver = new MessageDispatcher();
        receiver.register(Command.HEARTBEAT, new HeartBeatHandler());
        receiver.register(Command.HANDSHAKE, new HandshakeHandler());
        receiver.register(Command.BIND, new BindUserHandler());
        receiver.register(Command.UNBIND, new BindUserHandler());
        receiver.register(Command.FAST_CONNECT, new FastConnectHandler());
        receiver.register(Command.PUSH, new ClientPushHandler());
        receiver.register(Command.ACK, new AckHandler());
//        if (CC.mp.http.proxy_enabled) {
//            receiver.register(Command.HTTP_PROXY, new HttpProxyHandler());
//        }
        channelHandler = new ServerChannelHandler(true, connectionManager, receiver);

//        if (CC.mp.net.traffic_shaping.connect_server.enabled) {//启用流量整形，限流
//            trafficShapingExecutor = Executors.newSingleThreadScheduledExecutor(new NamedPoolThreadFactory(T_TRAFFIC_SHAPING));
//            trafficShapingHandler = new GlobalChannelTrafficShapingHandler(
//                    trafficShapingExecutor,
//                    write_global_limit, read_global_limit,
//                    write_channel_limit, read_channel_limit,
//                    check_interval);
//        }
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
