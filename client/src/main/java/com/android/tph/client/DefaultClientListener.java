package com.android.tph.client;

import com.android.tph.api.Client;
import com.android.tph.api.ClientListener;
import com.android.tph.util.thread.ExecutorManager;

import java.util.concurrent.Executor;

/**
 * Created by kiddo on 17-7-11.
 */

public class DefaultClientListener implements ClientListener {
    private final Executor executor = ExecutorManager.INSTANCE.getDispatchThread();
    private ClientListener listener;

    public void setListener(ClientListener listener){
        this.listener = listener;
    }

    @Override
    public void onConnected(final Client client) {
        if (listener != null){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onConnected(client);
                }
            });
        }
        client.fastConnect();
    }

    @Override
    public void onDisConnected(final Client client) {
        if (listener != null){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onDisConnected(client);
                }
            });
        }

    }

    @Override
    public void onHandshakeOk(Client client, int heartbeat) {

    }

    @Override
    public void onReceivePush(Client client, byte[] content, int messageId) {
        if (listener != null) {//dispatcher已经使用了Executor，此处直接同步调用
            listener.onReceivePush(client, content, messageId);
        }
    }

    @Override
    public void onKickUser(String deviceId, String userId) {

    }

    @Override
    public void onBind(boolean success, String userId) {

    }

    @Override
    public void onUnbind(boolean success, String userId) {

    }
}
