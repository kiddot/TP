package com.android.server.boot;


import com.android.server.boot.job.BootChain;
import com.android.server.boot.job.BootJob;
import com.android.server.boot.job.HttpProxyBoot;
import com.android.server.boot.job.PushCenterBoot;
import com.android.server.boot.job.ServerBoot;
import com.android.server.boot.job.ServiceRegistryBoot;
import com.android.server.core.server.GatewayServer;
import com.android.server.tools.config.CC;


/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public final class ServerLauncher {


//    private final BootChain chain = BootChain.chain();
//
//    public ServerLauncher() {
//        chain.boot()
//                //.setNext(new CacheManagerBoot())//1.初始化缓存模块
//                .setNext(new ServiceRegistryBoot())//2.启动服务注册与发现模块
//                //.setNext(new ServerBoot(ConnectionServer.I(), CS))//3.启动接入服务
//                //.setNext(() -> new ServerBoot(WebSocketServer.I(), WS), wsEnabled())//4.启动websocket接入服务
//                //.setNext(() -> new ServerBoot(GatewayUDPConnector.I(), GS), udpGateway())//5.启动udp网关服务
//                .setNext(new Supplier<BootJob>() {
//                    @Override
//                    public BootJob get() {
//                        return new ServerBoot(GatewayServer.I());
//                    }
//                }, CC.getInstance().tcpGateway())//6.启动tcp网关服务
//                //.setNext(new ServerBoot(AdminServer.I(), null))//7.启动控制台服务
//                .setNext(new PushCenterBoot())//8.启动推送中心组件
//                .setNext(new HttpProxyBoot())//9.启动http代理服务，dns解析服务
//                //.setNext(new MonitorBoot())//10.启动监控服务
//                .end();
//    }
//
    public void start() {
        GatewayServer gatewayServer = GatewayServer.I();
        gatewayServer.start();
    }

    public void stop() {
        GatewayServer gatewayServer = GatewayServer.I();
        gatewayServer.stop();
    }
}
