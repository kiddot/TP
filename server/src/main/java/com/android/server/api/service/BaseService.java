package com.android.server.api.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseService implements Service {

    protected final AtomicBoolean started = new AtomicBoolean();

    @Override
    public void init() {
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }

    protected void tryStart(Listener l, FunctionEx function) {
        FutureListener listener = wrap(l);
        if (started.compareAndSet(false, true)) {
            try {
                init();
                function.apply(listener);
                listener.monitor(this);
            } catch (Throwable e) {
                listener.onFailure(e);
                throw new ServiceException(e);
            }
        } else {
            listener.onFailure(new ServiceException("service already started."));
        }
    }

    protected void tryStop(Listener l, FunctionEx function) {
        FutureListener listener = wrap(l);
        if (started.compareAndSet(true, false)) {
            try {
                function.apply(listener);
                listener.monitor(this);
            } catch (Throwable e) {
                listener.onFailure(e);
                throw new ServiceException(e);
            }
        } else {
            listener.onFailure(new ServiceException("service already stopped."));
        }
    }

    public final CompletableFuture<Boolean> start() {
        FutureListener listener = new FutureListener(started);
        start(listener);
        return listener;
    }

    public final CompletableFuture<Boolean> stop() {
        FutureListener listener = new FutureListener(started);
        stop(listener);
        return listener;
    }

    @Override
    public final boolean syncStart() {
        return start().join();
    }

    @Override
    public final boolean syncStop() {
        return stop().join();
    }

    @Override
    public void start(Listener listener) {
        tryStart(listener, this::doStart);
    }

    @Override
    public void stop(Listener listener) {
        tryStop(listener, this::doStop);
    }

    protected void doStart(Listener listener) throws Throwable {
        listener.onSuccess();
    }

    protected void doStop(Listener listener) throws Throwable {
        listener.onSuccess();
    }

    protected interface FunctionEx {
        void apply(Listener l) throws Throwable;
    }

    /**
     * 防止Listener被重复执行
     *
     * @param l
     * @return
     */
    public FutureListener wrap(Listener l) {
        if (l == null) return new FutureListener(started);
        if (l instanceof FutureListener) return (FutureListener) l;
        return new FutureListener(l, started);
    }
}
