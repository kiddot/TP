package com.android.server.core.message;

/**
 * Created by kiddo on 17-7-15.
 */

public interface IPushMessage {

    boolean isBroadcast();

    String getUserId();

    int getClientType();

    byte[] getContent();

    boolean isNeedAck();

    byte getFlags();

    int getTimeoutMills();

}
