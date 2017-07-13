package com.android.server.boot.job;

import com.android.server.api.spi.net.DnsMappingManager;
import com.android.server.tools.config.CC;

public final class HttpProxyBoot extends BootJob {

    @Override
    protected void start() {
        if (CC.mp.http.proxy_enabled) {
            //NettyHttpClient.I().syncStart();
            DnsMappingManager.create().start();
        }

        startNext();
    }

    @Override
    protected void stop() {
        stopNext();
        if (CC.mp.http.proxy_enabled) {
            //NettyHttpClient.I().syncStop();
            DnsMappingManager.create().stop();
        }
    }
}
