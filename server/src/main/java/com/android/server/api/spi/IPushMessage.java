package com.android.server.api.spi;


import com.android.server.api.common.Condition;

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
