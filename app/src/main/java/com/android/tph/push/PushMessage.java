package com.android.tph.push;

/**
 * Created by kiddo on 17-7-11.
 */

public interface PushMessage {
    Integer getNid();

    String getMsgId();

    String getTicker();

    String getTitle();

    String getContent();

    Integer getNumber();

    Byte getFlags();

    String getLargeIcon();
}
