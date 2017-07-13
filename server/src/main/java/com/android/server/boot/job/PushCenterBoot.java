package com.android.server.boot.job;


import com.android.server.core.push.PushCenter;

/**
 * Created by ohun on 16/10/25.
 *
 * @author ohun@live.cn (夜色)
 */
public final class PushCenterBoot extends BootJob {
    @Override
    protected void start() {
        PushCenter.I.start();
        startNext();
    }

    @Override
    protected void stop() {
        stopNext();
        PushCenter.I.stop();
    }
}
