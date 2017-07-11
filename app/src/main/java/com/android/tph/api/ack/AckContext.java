package com.android.tph.api.ack;

import com.android.tph.api.protocol.Packet;

/**
 * Created by kiddo on 17-7-11.
 */

public class AckContext {
    public AckCallBack callback;
    public AckModel ackModel = AckModel.AUTO_ACK;
    public int timeout = 1000;
    public Packet request;
    public int retryCount;

    public static AckContext build(AckCallBack callback) {
        AckContext context = new AckContext();
        context.setCallback(callback);
        return context;
    }

    public AckCallBack getCallback() {
        return callback;
    }

    public AckContext setCallback(AckCallBack callback) {
        this.callback = callback;
        return this;
    }

    public AckModel getAckModel() {
        return ackModel;
    }

    public AckContext setAckModel(AckModel ackModel) {
        this.ackModel = ackModel;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public AckContext setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public Packet getRequest() {
        return request;
    }

    public AckContext setRequest(Packet request) {
        this.request = request;
        return this;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public AckContext setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }
}
