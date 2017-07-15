package com.android.server.core.task;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by kiddo on 17-7-15.
 */

public interface PushTask extends Runnable {
    ScheduledExecutorService getExecutor();

}
