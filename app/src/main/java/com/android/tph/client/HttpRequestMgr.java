package com.android.tph.client;

import com.android.tph.api.Logger;
import com.android.tph.api.http.HttpCallBack;
import com.android.tph.api.http.HttpRequest;
import com.android.tph.api.http.HttpResponse;
import com.android.tph.util.thread.ExecutorManager;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT;

/**
 * Created by kiddo on 17-7-11.
 */

public final class HttpRequestMgr {
    private static HttpRequestMgr I;
    private final Map<Integer, RequestTask> queue = new ConcurrentHashMap<>();
    private final ScheduledExecutorService timer = ExecutorManager.INSTANCE.getTimerThread();
    private final Executor executor = ExecutorManager.INSTANCE.getDispatchThread();
    //private final HttpResponse response404 = new HttpResponse(HTTP_NOT_FOUND, "Not Found", null, null);
    private final HttpResponse response408 = new HttpResponse(HTTP_CLIENT_TIMEOUT, "Request Timeout", null, null);
    private final Callable<HttpResponse> NONE = new Callable<HttpResponse>() {
        @Override
        public HttpResponse call() throws Exception {
            return response408;
        }
    };
    private final Logger logger = ClientConfig.I.getLogger();

    public static HttpRequestMgr I() {
        if (I == null) {
            synchronized (AckRequestMgr.class) {
                if (I == null) {
                    I = new HttpRequestMgr();
                }
            }
        }
        return I;
    }

    private HttpRequestMgr() {
    }

    public Future<HttpResponse> add(int sessionId, HttpRequest request) {
        RequestTask task = new RequestTask(sessionId, request);
        queue.put(sessionId, task);
        task.future = timer.schedule(task, task.timeout, TimeUnit.MILLISECONDS);
        return task;
    }

    public RequestTask getAndRemove(int sessionId) {
        return queue.remove(sessionId);
    }

    public final class RequestTask extends FutureTask<HttpResponse> implements Runnable {
        private HttpCallBack callback;
        private final String uri;
        private final int timeout;
        private final long sendTime;
        private final int sessionId;
        private Future<?> future;

        private RequestTask(int sessionId, HttpRequest request) {
            super(NONE);
            this.callback = request.getCallback();
            this.timeout = request.getTimeout();
            this.uri = request.uri;
            this.sendTime = System.currentTimeMillis();
            this.sessionId = sessionId;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean success = super.cancel(mayInterruptIfRunning);
            if (success) {
                if (future.cancel(true)) {
                    queue.remove(sessionId);
                    if (callback != null) {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                callback.onCancelled();
                            }
                        });
                        callback = null;
                    }
                }
            }
            logger.d("one request task cancelled, sessionId=%d, costTime=%d, uri=%s",
                    sessionId, (System.currentTimeMillis() - sendTime), uri);
            return success;
        }

        @Override
        public void run() {
            queue.remove(sessionId);
            setResponse(response408);
        }

        public void setResponse(HttpResponse response) {
            if (this.future.cancel(true)) {
                this.set(response);
                if (callback != null) {
                    callback.onResponse(response);
                }
                callback = null;
            }
            logger.d("one request task end, sessionId=%d, costTime=%d, response=%d, uri=%s",
                    sessionId, (System.currentTimeMillis() - sendTime), response.statusCode, uri);
        }
    }
}
