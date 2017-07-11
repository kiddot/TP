package com.android.tph.api.connection;

/**
 * Created by kiddo on 17-7-11.
 */

public class SessionContext {
    public int heartbeat;
    public Cipher cipher;
    public String bindUser;
    public String tags;

    public void changeCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public SessionContext setBindUser(String bindUser) {
        this.bindUser = bindUser;
        return this;
    }

    public SessionContext setTags(String tags) {
        this.tags = tags;
        return this;
    }

    public boolean handshakeOk() {
        return heartbeat > 0;
    }

    @Override
    public String toString() {
        return "SessionContext{" +
                "heartbeat=" + heartbeat +
                ", cipher=" + cipher +
                ", bindUser='" + bindUser + '\'' +
                '}';
    }
}
