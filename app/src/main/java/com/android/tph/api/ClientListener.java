package com.android.tph.api;

/**
 * Created by kiddo on 17-7-11.
 */

public interface ClientListener {
    void onConnected(Client client);

    void onDisConnected(Client client);

    void onHandshakeOk(Client client, int heartbeat);

    void onReceivePush(Client client, byte[] content, int messageId);

    void onKickUser(String deviceId, String userId);

    void onBind(boolean success, String userId);

    void onUnbind(boolean success, String userId);
}
