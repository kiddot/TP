package com.android.tph.api.ack;

import com.android.tph.api.protocol.Packet;

/**
 * Created by kiddo on 17-7-11.
 */

public interface AckCallBack {
    void onSuccess(Packet response);

    void onTimeout(Packet request);
}
