package com.android.tph.api.protocol;

import com.android.tph.api.http.HttpRequest;
import com.android.tph.api.http.HttpResponse;
import com.android.tph.api.push.PushContext;

import java.util.concurrent.Future;

/**
 * Created by kiddo on 17-7-11.
 */

public interface PushProtocol {
    /**
     * 健康检查, 检测读写超时, 发送心跳
     *
     * @return true/false Client
     */
    boolean healthCheck();

    void fastConnect();

    void handshake();

    void bindUser(String userId, String tags);

    void unbindUser();

    void ack(int messageId);

    Future<Boolean> push(PushContext context);

    Future<Boolean> push(PushContext context, String userId);

    Future<HttpResponse> sendHttp(HttpRequest request);
}
