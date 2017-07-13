package com.android.server.core.push;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by ohun on 16/10/24.
 *
 * @author ohun@live.cn (夜色)
 */
public interface PushTask extends Runnable {
    ScheduledExecutorService getExecutor();
}
