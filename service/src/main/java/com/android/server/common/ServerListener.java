package com.android.server.common;

/**
 * Created by kiddo on 17-7-14.
 */

public interface ServerListener {
    void onSuccess(Object... args);

    void onFailure(Throwable cause);
}
