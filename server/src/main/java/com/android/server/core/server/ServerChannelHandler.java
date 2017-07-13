package com.android.server.core.server;


import com.android.server.api.PacketReceiver;
import com.android.server.api.connection.Connection;
import com.android.server.api.connection.ConnectionManager;
import com.android.server.api.event.ConnectionCloseEvent;
import com.android.server.api.protocol.Command;
import com.android.server.api.protocol.Packet;
import com.android.server.netty.connection.NettyConnection;
import com.android.server.tools.EventBus;
import com.android.server.tools.common.Profiler;
import com.android.server.tools.config.CC;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public final class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerChannelHandler.class);

    private static final long profile_slowly_limit = CC.mp.monitor.profile_slowly_duration.toMillis();

    private final boolean security; //是否启用加密
    private final ConnectionManager connectionManager;
    private final PacketReceiver receiver;

    public ServerChannelHandler(boolean security, ConnectionManager connectionManager, PacketReceiver receiver) {
        this.security = security;
        this.connectionManager = connectionManager;
        this.receiver = receiver;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packet packet = (Packet) msg;
        byte cmd = packet.cmd;

        try {
            Profiler.start("time cost on [channel read]: ", packet.toString());
            Connection connection = connectionManager.get(ctx.channel());
            LOGGER.debug("channelRead conn={}, packet={}", ctx.channel(), connection.getSessionContext());
            connection.updateLastReadTime();
            receiver.onReceive(packet, connection);
        } finally {
            Profiler.release();
            if (Profiler.getDuration() > profile_slowly_limit) {
                LOGGER.debug("Read Packet[cmd={}] Slowly: \n{}", Command.toCMD(cmd), Profiler.dump());
            }
            Profiler.reset();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Connection connection = connectionManager.get(ctx.channel());
        LOGGER.debug("client caught ex, conn={}", connection);
        LOGGER.error("caught an ex, channel={}, conn={}", ctx.channel(), connection);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.debug("client connected conn={}", ctx.channel());
        Connection connection = new NettyConnection();
        connection.init(ctx.channel(), security);
        connectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = connectionManager.removeAndClose(ctx.channel());
        EventBus.I.post(new ConnectionCloseEvent(connection));
        LOGGER.debug("client disconnected conn={}", connection);
    }
}