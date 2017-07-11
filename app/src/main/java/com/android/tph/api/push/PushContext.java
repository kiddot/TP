package com.android.tph.api.push;

import com.android.tph.api.Constants;
import com.android.tph.api.ack.AckModel;

/**
 * Created by kiddo on 17-7-11.
 */

public class PushContext {
    public byte[] content;
    public AckModel ackModel;

    public PushContext(byte[] content) {
        this.content = content;
    }

    public static PushContext build(byte[] content) {
        return new PushContext(content);
    }

    public static PushContext build(String content) {
        return new PushContext(content.getBytes(Constants.UTF_8));
    }

    public byte[] getContent() {
        return content;
    }

    public PushContext setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public AckModel getAckModel() {
        return ackModel;
    }

    public PushContext setAckModel(AckModel ackModel) {
        this.ackModel = ackModel;
        return this;
    }
}
