package com.android.tph.client;

import com.android.tph.api.Logger;
import com.android.tph.api.ack.AckCallBack;
import com.android.tph.api.ack.AckContext;
import com.android.tph.api.ack.AckModel;
import com.android.tph.api.connection.Connection;
import com.android.tph.api.protocol.Packet;
import com.android.tph.util.thread.ExecutorManager;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by kiddo on 17-7-11.
 */

public class AckRequestMgr {
    private static AckRequestMgr I;

    private final Logger logger = ClientConfig.I.getLogger();

    private final Map<Integer, RequestTask> queue = new ConcurrentHashMap<>();
    private final ScheduledExecutorService timer = ExecutorManager.INSTANCE.getTimerThread();
    private final Callable<Boolean> NONE = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            return Boolean.FALSE;
        }
    };
    private Connection connection;


    public static AckRequestMgr I() {
        if (I == null) {
            synchronized (AckRequestMgr.class) {
                if (I == null) {
                    I = new AckRequestMgr();
                }
            }
        }
        return I;
    }

    private AckRequestMgr() {
    }

    public Future<Boolean> add(int sessionId, AckContext context) {
        if (context.ackModel == AckModel.NO_ACK) return null;
        if (context.callback == null) return null;
        return addTask(new RequestTask(sessionId, context));
    }

    public RequestTask getAndRemove(int sessionId) {
        return queue.remove(sessionId);
    }


    public void clear() {
        for (RequestTask task : queue.values()) {
            try {
                task.future.cancel(true);
            } catch (Exception e) {
            }
        }
    }

    private RequestTask addTask(RequestTask task) {
        queue.put(task.sessionId, task);
        task.future = timer.schedule(task, task.timeout, TimeUnit.MILLISECONDS);//定时执行周期任务
        return task;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public final class RequestTask extends FutureTask<Boolean> implements Runnable {
        private final int timeout;
        private final long sendTime;
        private final int sessionId;
        private AckCallBack callback;
        private Packet request;
        private Future<?> future;
        private int retryCount;

        private RequestTask(AckCallBack callback, int timeout, int sessionId, Packet request, int retryCount) {
            super(NONE);
            this.callback = callback;
            this.timeout = timeout;
            this.sendTime = System.currentTimeMillis();
            this.sessionId = sessionId;
            this.request = request;
            this.retryCount = retryCount;
        }

        private RequestTask(int sessionId, AckContext context) {
            this(context.callback, context.timeout, sessionId, context.request, context.retryCount);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void run() {
            queue.remove(sessionId);
            timeout();
        }

        public void timeout() {
            call(null);
        }

        public void success(Packet packet) {
            call(packet);
        }

        private void call(Packet response) {
            if (this.future.cancel(true)) {
                boolean success = response != null;
                this.set(success);
                if (callback != null) {
                    if (success) {
                        logger.d("receive one ack response, sessionId=%d, costTime=%d, request=%s, response=%s"
                                , sessionId, (System.currentTimeMillis() - sendTime), request, response
                        );
                        callback.onSuccess(response);
                    } else if (request != null && retryCount > 0) {
                        logger.w("one ack request timeout, retry=%d, sessionId=%d, costTime=%d, request=%s"
                                , retryCount, sessionId, (System.currentTimeMillis() - sendTime), request
                        );
                        addTask(copy(retryCount - 1));
                        connection.send(request);
                    } else {
                        logger.w("one ack request timeout, sessionId=%d, costTime=%d, request=%s"
                                , sessionId, (System.currentTimeMillis() - sendTime), request
                        );
                        callback.onTimeout(request);
                    }
                }
                callback = null;
                request = null;
            }
        }

        private RequestTask copy(int retryCount) {
            return new RequestTask(callback, timeout, sessionId, request, retryCount);
        }
    }
}
