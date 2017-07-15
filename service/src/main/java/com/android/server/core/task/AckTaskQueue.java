package com.android.server.core.task;

import com.android.server.common.BaseService;
import com.android.server.common.ServerListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
/**
 * Created by kiddo on 17-7-15.
 */

public class AckTaskQueue extends BaseService {
    private final Logger logger = LoggerFactory.getLogger(AckTaskQueue.class);

    private static final int DEFAULT_TIMEOUT = 3000;
    public static final AckTaskQueue I = new AckTaskQueue();

    private final ConcurrentMap<Integer, AckTask> queue = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduledExecutor;

    private AckTaskQueue() {
    }

    public void add(AckTask task, int timeout) {
        queue.put(task.ackMessageId, task);

        //使用 task.getExecutor() 并没更快
        task.setFuture(scheduledExecutor.schedule(task,
                timeout > 0 ? timeout : DEFAULT_TIMEOUT,
                TimeUnit.MILLISECONDS
        ));

        logger.debug("one ack task add to queue, task={}, timeout={}", task, timeout);
    }

    public AckTask getAndRemove(int sessionId) {
        return queue.remove(sessionId);
    }

    @Override
    protected void doStart(ServerListener listener) throws Throwable {
        scheduledExecutor = Executors.newScheduledThreadPool(10);
        super.doStart(listener);
    }

    @Override
    protected void doStop(ServerListener listener) throws Throwable {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        super.doStop(listener);
    }

    @Override
    public boolean syncStart() {
        return true;
    }

    @Override
    public boolean syncStop() {
        return true;
    }
}
