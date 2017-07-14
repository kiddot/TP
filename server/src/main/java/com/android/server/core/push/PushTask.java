package com.android.server.core.push;

import java.util.concurrent.ScheduledExecutorService;

public interface PushTask extends Runnable {
    ScheduledExecutorService getExecutor();
}
