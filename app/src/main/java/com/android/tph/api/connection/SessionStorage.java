package com.android.tph.api.connection;

/**
 * Created by kiddo on 17-7-11.
 */

public interface SessionStorage {
    void saveSession(String sessionContext);

    String getSession();

    void clearSession();
}
