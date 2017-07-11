package com.android.tph.api;

import com.android.tph.api.protocol.PushProtocol;

/**
 * Created by kiddo on 17-7-11.
 */

public interface Client extends PushProtocol {
    void start();

    void stop();

    void destroy();

    boolean isRunning();

    void onNetStateChange(boolean isConnected);
}
