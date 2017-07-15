package com.android.server.boot.job;

/**
 * Created by kiddo on 17-7-15.
 */

public abstract class BootJob {

    protected BootJob next;

    protected abstract void start();

    protected abstract void stop();

    public void startNext() {
        if (next != null) {
            next.start();
        }
    }

    public void stopNext() {
        if (next != null) {
            next.stop();
        }
    }

    public BootJob setNext(BootJob next) {
        this.next = next;
        return next;
    }

    protected String getNextName() {
        return next.getName();
    }

    protected String getName() {
        return this.getClass().getSimpleName();
    }

}
