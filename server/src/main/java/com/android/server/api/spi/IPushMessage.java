package com.android.server.api.spi;


import java.util.concurrent.locks.Condition;

public interface IPushMessage {

    boolean isBroadcast();

    String getUserId();

    int getClientType();

    byte[] getContent();

    boolean isNeedAck();

    byte getFlags();

    int getTimeoutMills();

    String getTaskId() ;

    Condition getCondition() ;

    void finalized() ;

}
