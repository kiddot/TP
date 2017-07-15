package com.android.server.core;

import com.android.server.common.BaseService;
import com.android.server.common.ServerListener;
import com.android.server.core.connection.Connection;
import com.android.server.core.message.IPushMessage;
import com.android.server.core.message.MessagePusher;
import com.android.server.core.task.AckTaskQueue;
import com.android.server.core.task.PushTask;
import com.android.server.core.task.SingleUserPushTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kiddo on 17-7-15.
 */

public final class PushCenter extends BaseService implements MessagePusher {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final PushCenter I = new PushCenter();


    private final AtomicLong taskNum = new AtomicLong();

    //private final PushListener<IPushMessage> pushListener = PushListenerFactory.create();

    private PushTaskExecutor executor;

    @Override
    public void push(IPushMessage message, Connection connection) {
        addTask(new SingleUserPushTask(message, connection));
    }

    public void addTask(PushTask task) {
        executor.addTask(task);
        logger.debug("add new task to push center, count={}, task={}", taskNum.incrementAndGet(), task);
    }

    public void delayTask(long delay, PushTask task) {
        executor.delayTask(delay, task);
        logger.debug("delay task to push center, count={}, task={}", taskNum.incrementAndGet(), task);
    }

    @Override
    protected void doStart(ServerListener listener) throws Throwable {
//        if (CC.mp.net.udpGateway() || CC.mp.thread.pool.push_task > 0) {
//            executor = new CustomJDKExecutor();
//        } else {//实际情况使用EventLoo并没有更快，还有待测试
//            executor = new NettyEventLoopExecutor();
//        }

        //MBeanRegistry.getInstance().register(new PushCenterBean(taskNum), null);
        AckTaskQueue.I.start(new ServerListener() {
            @Override
            public void onSuccess(Object... args) {

            }

            @Override
            public void onFailure(Throwable cause) {

            }
        });
        logger.info("push center start success");
        listener.onSuccess();
    }

    @Override
    protected void doStop(ServerListener listener) throws Throwable {
        executor.shutdown();
        AckTaskQueue.I.stop(new ServerListener() {
            @Override
            public void onSuccess(Object... args) {

            }

            @Override
            public void onFailure(Throwable cause) {

            }
        });
        logger.info("push center stop success");
        listener.onSuccess();
    }

    @Override
    public boolean syncStart() {
        return true;
    }

    @Override
    public boolean syncStop() {
        return true;
    }


    /**
     * TCP 模式直接使用GatewayServer work 线程池
     */
    private static class NettyEventLoopExecutor implements PushTaskExecutor {

        @Override
        public void shutdown() {
        }

        @Override
        public void addTask(PushTask task) {
            task.getExecutor().execute(task);
        }

        @Override
        public void delayTask(long delay, PushTask task) {
            task.getExecutor().schedule(task, delay, TimeUnit.NANOSECONDS);
        }
    }


    /**
     * UDP 模式使用自定义线程池
     */
    private static class CustomJDKExecutor implements PushTaskExecutor {
        private final ScheduledExecutorService executorService;

        private CustomJDKExecutor(ScheduledExecutorService executorService) {
            this.executorService = executorService;
        }

        @Override
        public void shutdown() {
            executorService.shutdown();
        }

        @Override
        public void addTask(PushTask task) {
            executorService.execute(task);
        }

        @Override
        public void delayTask(long delay, PushTask task) {
            executorService.schedule(task, delay, TimeUnit.NANOSECONDS);
        }
    }

    private interface PushTaskExecutor {

        void shutdown();

        void addTask(PushTask task);

        void delayTask(long delay, PushTask task);
    }

}
