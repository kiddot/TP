package com.android.server.boot;

import com.android.server.boot.job.BootChain;
import com.android.server.boot.job.PushCenterBoot;
import com.android.server.boot.job.ServerBoot;
import com.android.server.core.server.ConnectionServer;

/**
 * Created by kiddo on 17-7-15.
 */

public class ServerLauncher {
    private final BootChain chain = BootChain.chain();


    public ServerLauncher(){
        chain.boot()
                .setNext(new ServerBoot(ConnectionServer.I()))
                .setNext(new PushCenterBoot())
                .end();
    }

    public void start() {
        chain.start();
    }

    public void stop() {
        chain.stop();
    }
}
