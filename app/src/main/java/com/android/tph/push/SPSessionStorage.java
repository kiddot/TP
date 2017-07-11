package com.android.tph.push;

import android.content.SharedPreferences;

import com.android.tph.api.connection.SessionStorage;

/**
 * Created by kiddo on 17-7-11.
 */

public class SPSessionStorage implements SessionStorage{
    private final SharedPreferences sp;

    public SPSessionStorage(SharedPreferences sp) {
        this.sp = sp;
    }

    @Override
    public void saveSession(String sessionContext) {
        sp.edit().putString("session", sessionContext).apply();
    }

    @Override
    public String getSession() {
        return sp.getString("session", null);
    }

    @Override
    public void clearSession() {
        sp.edit().remove("session").apply();
    }
}
