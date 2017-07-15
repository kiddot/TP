package com.android.server.common;

import com.android.server.common.exception.ServiceException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kiddo on 17-7-14.
 */

public abstract class BaseService implements Service{
    protected final AtomicBoolean started = new AtomicBoolean();

    @Override
    public void init() {
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }

    protected void tryStart(ServerListener l) {
        //FutureListener listener = wrap(l);
        if (started.compareAndSet(false, true)) {
            try {
                init();
                //function.apply(listener);
                //listener.monitor(this);
            } catch (Throwable e) {
                ///listener.onFailure(e);
                throw new ServiceException(e);
            }
        } else {
            //listener.onFailure(new ServiceException("service already started."));
        }
    }

    protected void tryStop(ServerListener l) {
       // FutureListener listener = wrap(l);
        if (started.compareAndSet(true, false)) {
            try {
                //function.apply(listener);
                //listener.monitor(this);
            } catch (Throwable e) {
                //listener.onFailure(e);
                throw new ServiceException(e);
            }
        } else {
            //listener.onFailure(new ServiceException("service already stopped."));
        }
    }

    @Override
    public void start(ServerListener listener) {
        tryStart(listener);
    }

    @Override
    public void stop(ServerListener listener) {
        tryStop(listener);
    }

    protected interface FunctionEx {
        void apply(ServerListener l) throws Throwable;
    }

    protected void doStart(ServerListener listener) throws Throwable {
        listener.onSuccess();
    }

    protected void doStop(ServerListener listener) throws Throwable {
        listener.onSuccess();
    }

}
