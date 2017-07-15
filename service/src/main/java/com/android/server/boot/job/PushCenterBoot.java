package com.android.server.boot.job;

import com.android.server.common.ServerListener;
import com.android.server.core.PushCenter;

/**
 * Created by kiddo on 17-7-15.
 */

public class PushCenterBoot extends BootJob {
    @Override
    protected void start() {
        PushCenter.I.start(new ServerListener() {
            @Override
            public void onSuccess(Object... args) {

            }

            @Override
            public void onFailure(Throwable cause) {

            }
        });
        startNext();
    }

    @Override
    protected void stop() {
        stopNext();
        PushCenter.I.stop(new ServerListener() {
            @Override
            public void onSuccess(Object... args) {

            }

            @Override
            public void onFailure(Throwable cause) {

            }
        });
    }
}
