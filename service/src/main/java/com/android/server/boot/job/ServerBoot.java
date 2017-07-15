package com.android.server.boot.job;

import com.android.server.common.ServerListener;
import com.android.server.common.Service;

/**
 * Created by kiddo on 17-7-15.
 */

public class ServerBoot extends BootJob {
    private final Service server;

    public ServerBoot(Service server) {
        this.server = server;
    }

    @Override
    public void start() {
        server.init();
        server.start(new ServerListener() {
            @Override
            public void onSuccess(Object... args) {
                startNext();
            }

            @Override
            public void onFailure(Throwable cause) {
                System.exit(-1);
            }
        });
    }

    @Override
    protected void stop() {
        stopNext();
        server.stop(new ServerListener() {
            @Override
            public void onSuccess(Object... args) {

            }

            @Override
            public void onFailure(Throwable cause) {

            }
        });
    }

    @Override
    protected String getName() {
        return super.getName() + '(' + server.getClass().getSimpleName() + ')';
    }
}
