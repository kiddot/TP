package com.android.server.core.task;

import com.android.server.core.callback.AckCallback;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
/**
 * Created by kiddo on 17-7-15.
 */

public class AckTask implements Runnable {
    final int ackMessageId;

    private AckCallback callback;
    private Future<?> timeoutFuture;

    public AckTask(int ackMessageId) {
        this.ackMessageId = ackMessageId;
    }

    public static AckTask from(int ackMessageId) {
        return new AckTask(ackMessageId);
    }

    public void setFuture(Future<?> future) {
        this.timeoutFuture = future;
    }

    public AckTask setCallback(AckCallback callback) {
        this.callback = callback;
        return this;
    }

    private boolean tryDone() {
        return timeoutFuture.cancel(true);
    }

    public void onResponse() {
        if (tryDone()) {
            callback.onSuccess(this);
            callback = null;
        }
    }

    public void onTimeout() {
        AckTask context = AckTaskQueue.I.getAndRemove(ackMessageId);
        if (context != null && tryDone()) {
            callback.onTimeout(this);
            callback = null;
        }
    }

    @Override
    public String toString() {
        return "{" +
                ", ackMessageId=" + ackMessageId +
                '}';
    }

    @Override
    public void run() {
        onTimeout();
    }
}
