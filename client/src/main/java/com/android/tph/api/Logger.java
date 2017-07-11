package com.android.tph.api;

/**
 * Created by kiddo on 17-7-11.
 */

public interface Logger {
    void enable(boolean enabled);

    void d(String s, Object... args);

    void i(String s, Object... args);

    void w(String s, Object... args);

    void e(Throwable e, String s, Object... args);
}
