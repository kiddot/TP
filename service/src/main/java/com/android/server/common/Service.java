package com.android.server.common;

/**
 * Created by kiddo on 17-7-14.
 */

public interface Service {
    void start(ServerListener listener);

    void stop(ServerListener listener);

    boolean syncStart();

    boolean syncStop();

    void init();

    boolean isRunning();
}
