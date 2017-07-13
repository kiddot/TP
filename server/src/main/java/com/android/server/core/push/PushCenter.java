package com.android.server.core.push;

import com.android.server.api.connection.Connection;
import com.android.server.api.service.BaseService;
import com.android.server.api.service.Listener;
import com.android.server.api.spi.IPushMessage;
import com.android.server.api.spi.push.MessagePusher;
import com.android.server.tools.config.CC;
import com.android.server.tools.thread.pool.ThreadPoolManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.android.server.tools.config.CC.mp.push.flow_control.global.duration;
import static com.android.server.tools.config.CC.mp.push.flow_control.global.limit;
import static com.android.server.tools.config.CC.mp.thread.pool.event_bus.max;

public final class PushCenter extends BaseService implements MessagePusher {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final PushCenter I = new PushCenter();

//    private final GlobalFlowControl globalFlowControl = new GlobalFlowControl(
//            limit, CC.mp.push.flow_control.global.max, duration
//    );

    private final AtomicLong taskNum = new AtomicLong();

    //private final PushListener<IPushMessage> pushListener = PushListenerFactory.create();

    private PushTaskExecutor executor;

    @Override
    public void push(IPushMessage message, Connection connection) {
//        if (message.isBroadcast()) {
//            FlowControl flowControl = (message.getTaskId() == null)
//                    ? new FastFlowControl(limit, max, duration)
//                    : new RedisFlowControl(message.getTaskId(), max);
//            addTask(new BroadcastPushTask(message, flowControl));
//        } else {
            addTask(new SingleUserPushTask(message, connection));
        //}
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
    protected void doStart(Listener listener) throws Throwable {
        if (CC.mp.net.udpGateway() || CC.mp.thread.pool.push_task > 0) {
            executor = new CustomJDKExecutor(ThreadPoolManager.I.getPushTaskTimer());
        } else {//实际情况使用EventLoo并没有更快，还有待测试
            executor = new NettyEventLoopExecutor();
        }

//        MBeanRegistry.getInstance().register(new PushCenterBean(taskNum), null);
//        AckTaskQueue.I.start();
        logger.info("push center start success");
        listener.onSuccess();
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        executor.shutdown();
        //AckTaskQueue.I.stop();
        logger.info("push center stop success");
        listener.onSuccess();
    }

//    public PushListener<IPushMessage> getPushListener() {
//        return pushListener;
//    }


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

//    @Spi(order = -1)
//    public static final class CoreMessagePusherFactory implements MessagePusherFactory {
//        @Override
//        public MessagePusher get() {
//            return PushCenter.I;
//        }
//    }
}
