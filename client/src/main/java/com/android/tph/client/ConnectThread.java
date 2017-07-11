package com.android.tph.client;

import com.android.tph.util.thread.EventLock;
import com.android.tph.util.thread.ExecutorManager;

import java.util.concurrent.Callable;

/**
 * Created by kiddo on 17-7-11.
 */

public class ConnectThread extends Thread {
    private volatile Callable<Boolean> runningTask;
    private volatile boolean runningFlag = true;
    private final EventLock connLock;
    public ConnectThread(EventLock connLock) {
        this.connLock = connLock;
        this.setName(ExecutorManager.START_THREAD_NAME);
        this.start();
    }

    public synchronized void addConnectTask(Callable<Boolean> task) {
        Callable<Boolean> oldTask = runningTask;
        if (oldTask != null) {
            this.interrupt();
        }
        runningTask = task;
        this.notify();
    }

    public synchronized void shutdown() {
        this.runningFlag = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (runningFlag) {
            try {
                synchronized (this) {
                    while (runningTask == null) {
                        this.wait();
                    }
                }
                if (runningTask.call()) {
                    break;
                }
            } catch (InterruptedException e) {
                continue;
            } catch (Exception e) {
                ClientConfig.I.getLogger().e(e, "run connect task error");
                break;
            }
        }
        //connLock.broadcast();
    }
}
