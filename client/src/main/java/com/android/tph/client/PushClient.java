package com.android.tph.client;

import com.android.tph.api.Client;
import com.android.tph.api.Logger;
import com.android.tph.api.ack.AckCallBack;
import com.android.tph.api.ack.AckContext;
import com.android.tph.api.ack.AckModel;
import com.android.tph.api.connection.SessionContext;
import com.android.tph.api.connection.SessionStorage;
import com.android.tph.api.http.HttpRequest;
import com.android.tph.api.http.HttpResponse;
import com.android.tph.api.protocol.Command;
import com.android.tph.api.protocol.Packet;
import com.android.tph.api.push.PushContext;
import com.android.tph.handler.HttpProxyHandler;
import com.android.tph.message.AckMessage;
import com.android.tph.message.BindUserMessage;
import com.android.tph.message.FastConnectMessage;
import com.android.tph.message.HandshakeMessage;
import com.android.tph.message.HttpRequestMessage;
import com.android.tph.message.PushMessage;
import com.android.tph.security.AesCipher;
import com.android.tph.security.CipherBox;
import com.android.tph.session.PersistentSession;
import com.android.tph.util.Strings;
import com.android.tph.util.thread.ExecutorManager;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.android.tph.api.Constants.MAX_HB_TIMEOUT_COUNT;

/**
 * Created by kiddo on 17-7-11.
 */

public class PushClient implements Client, AckCallBack {
    private enum State {Started, Shutdown, Destroyed}

    private final AtomicReference<State> clientState = new AtomicReference<>(State.Shutdown);

    private final TCPConnection connection;
    private final ClientConfig config;
    private final Logger logger;
    private int hbTimeoutTimes;

    private AckRequestMgr ackRequestMgr;
    private HttpRequestMgr httpRequestMgr;

    public PushClient(ClientConfig config) {
        this.config = config;
        this.logger = config.getLogger();

        MessageDispatcher receiver = new MessageDispatcher();

        if (config.isEnableHttpProxy()) {
            this.httpRequestMgr = HttpRequestMgr.I();
            receiver.register(Command.HTTP_PROXY, new HttpProxyHandler());
        }

        this.ackRequestMgr = AckRequestMgr.I();
        this.connection = new TCPConnection(this, receiver);
        this.ackRequestMgr.setConnection(this.connection);
    }

    @Override
    public void start() {
        if (clientState.compareAndSet(State.Shutdown, State.Started)) {
            connection.setAutoConnect(true);
            connection.connect();
            logger.w("do start client ...");
        }
    }

    @Override
    public void stop() {
        logger.w("client shutdown !!!, state=%s", clientState.get());
        if (clientState.compareAndSet(State.Started, State.Shutdown)) {
            connection.setAutoConnect(false);
            connection.close();
        }
    }

    @Override
    public void destroy() {
        if (clientState.get() != State.Destroyed) {
            this.stop();
            logger.w("client destroy !!!");
            ExecutorManager.INSTANCE.shutdown();
            ClientConfig.I.destroy();
            clientState.set(State.Destroyed);
        }
    }

    @Override
    public boolean isRunning() {
        return clientState.get() == State.Started && connection.isConnected();
    }

    /**
     * 这个方法主要用于解决网络十分不稳定的场景：
     * 正常情况如果网络断开，就应该关闭连接，反之则应去建立连接
     * 但是在网络抖动厉害时就会发生连接频繁的建立／断开。
     * <p>
     * 处理这种场景的其中一个方案是：
     * 当网络断开时不主动关闭连接，而是尝试发送一次心跳检测，
     * 如果能收到响应，说明网络短时间内又恢复了，
     * 否则就断开连接，等待网络恢复并重建连接。
     *
     * @param isConnected true/false
     */
    @Override
    public void onNetStateChange(boolean isConnected) {
        connection.setAutoConnect(isConnected);
        logger.i("network state change, isConnected=%b, connection=%s", isConnected, connection);
        if (isConnected) { //当有网络时，去尝试重连
            connection.connect();
        } else if (connection.isConnected()) { //无网络，如果连接没有断开，尝试发送一次心跳检测，用于快速校验网络状况
            connection.resetTimeout();//心跳检测前，重置上次读写数据包的时间戳
            hbTimeoutTimes = MAX_HB_TIMEOUT_COUNT - 2;//总共要调用两次healthCheck，第一次用于发送心跳，第二次用于检测是否超时
            final ScheduledExecutorService timer = ExecutorManager.INSTANCE.getTimerThread();

            //隔3s后发送一次心跳检测，看能不能收到服务端的响应
            timer.schedule(new Runnable() {
                int checkCount = 0;

                @Override
                public void run() {
                    logger.w("network disconnected, try test tcp connection checkCount=%d, connection=%s", checkCount, connection);
                    //如果期间连接状态发生变化，取消任务
                    if (connection.isAutoConnect() || !connection.isConnected()) return;

                    if (++checkCount <= 2) {
                        if (healthCheck() && checkCount < 2) {
                            timer.schedule(this, 3, TimeUnit.SECONDS);
                        }
                    }
                }

            }, 3, TimeUnit.SECONDS);
        }
    }

    @Override
    public boolean healthCheck() {

        if (connection.isReadTimeout()) {
            hbTimeoutTimes++;
            logger.w("heartbeat timeout times=%s", hbTimeoutTimes);
        } else {
            hbTimeoutTimes = 0;
        }

        if (hbTimeoutTimes >= MAX_HB_TIMEOUT_COUNT) {
            logger.w("heartbeat timeout times=%d over limit=%d, client restart", hbTimeoutTimes, MAX_HB_TIMEOUT_COUNT);
            hbTimeoutTimes = 0;
            connection.reconnect();
            return false;
        }

        if (connection.isWriteTimeout()) {
            logger.d("<<< send heartbeat ping...");
            connection.send(Packet.HB_PACKET);
        }

        return true;
    }

    @Override
    public void fastConnect() {
        SessionStorage storage = config.getSessionStorage();
        if (storage == null) {
            handshake();
            return;
        }

        String ss = storage.getSession();
        if (Strings.isBlank(ss)) {
            handshake();
            return;
        }

        PersistentSession session = PersistentSession.decode(ss);
        if (session == null || session.isExpired()) {
            storage.clearSession();
            logger.w("fast connect failure session expired, session=%s", session);
            handshake();
            return;
        }

        FastConnectMessage message = new FastConnectMessage(connection);
        message.deviceId = config.getDeviceId();
        message.sessionId = session.sessionId;
        message.maxHeartbeat = config.getMaxHeartbeat();
        message.minHeartbeat = config.getMinHeartbeat();
        message.encodeBody();
        ackRequestMgr.add(message.getSessionId(), AckContext
                .build(this)
                .setRequest(message.getPacket())
                .setTimeout(1000)
                .setRetryCount(3)
        );
        logger.w("<<< do fast connect, message=%s", message);
        message.sendRaw();
        connection.getSessionContext().changeCipher(session.cipher);
    }

    @Override
    public void handshake() {
        SessionContext context = connection.getSessionContext();
        //context.changeCipher(CipherBox.INSTANCE.getRsaCipher());//TODO: 暂时不加密
        HandshakeMessage message = new HandshakeMessage(connection);
        message.clientKey = CipherBox.INSTANCE.randomAESKey();
        message.iv = CipherBox.INSTANCE.randomAESIV();
        message.deviceId = config.getDeviceId();
        message.osName = config.getOsName();
        message.osVersion = config.getOsVersion();
        message.clientVersion = config.getClientVersion();
        message.maxHeartbeat = config.getMaxHeartbeat();
        message.minHeartbeat = config.getMinHeartbeat();
        message.encodeBody();
        ackRequestMgr.add(message.getSessionId(), AckContext
                .build(this)
                .setTimeout(5000)
                .setRequest(message.getPacket())
                .setRetryCount(3)
        );
        logger.w("<<< do handshake, message=%s", message);
        message.send();
        //context.changeCipher(new AesCipher(message.clientKey, message.iv));
    }

    @Override
    public void bindUser(final String userId, final String tags) {
        if (Strings.isBlank(userId)) {
            logger.w("bind user is null");
            return;
        }
        SessionContext context = connection.getSessionContext();
        if (context.bindUser != null) {
            if (userId.equals(context.bindUser)) {//已经绑定
                if (tags != null && tags.equals(context.tags)) return;
            } else {
                unbindUser();//切换用户，要先解绑老用户
            }
        }
        context.setBindUser(userId).setTags(tags);
        config.setUserId(userId).setTags(tags);
        BindUserMessage message = BindUserMessage
                .buildBind(connection)
                .setUserId(userId)
                .setAlias("liangdekai")
                .setTags(tags);
        message.encodeBody();
        ackRequestMgr.add(message.getSessionId(), AckContext
                .build(this)
                .setTimeout(3000)
                .setRequest(message.getPacket())
                .setRetryCount(3)
        );
        logger.w("<<< do bind user, userId=%s", userId);
        message.send();
        logger.w("bindMessage : " + message);
    }

    @Override
    public void unbindUser() {
        String userId = config.getUserId();
        if (Strings.isBlank(userId)) {
            logger.w("unbind user is null");
            return;
        }
        config.setUserId(null).setTags(null);
        connection.getSessionContext().setBindUser(null).setTags(null);
        BindUserMessage
                .buildUnbind(connection)
                .setUserId(userId)
                .send();
        logger.w("<<< do unbind user, userId=%s", userId);
    }

    @Override
    public void ack(int messageId) {
        if (messageId > 0) {
            AckMessage ackMessage = new AckMessage(messageId, connection);
            ackMessage.sendRaw();
            logger.d("<<< send ack for push messageId=%d", messageId);
        }
    }

    @Override
    public Future<Boolean> push(PushContext context) {
        if (connection.getSessionContext().handshakeOk()) {
            PushMessage message = new PushMessage(context.content, connection);
            if (context.ackModel != null ) {
                message.addFlag(context.ackModel.flag);
            }
            message.addFlag(Packet.FLAG_AUTO_ACK);
            message.send();
            logger.d("<<< send push message=%s", message);
            return ackRequestMgr.add(message.getSessionId(), context);
        }
        return null;
    }

    @Override
    public Future<Boolean> push(PushContext context, String userId) {
        if (connection.getSessionContext().handshakeOk()) {
            PushMessage message = new PushMessage(context.content, connection);
            if (context.ackModel != null ) {
                message.addFlag(context.ackModel.flag);
            }
            message.setUserId(userId);
            message.addFlag(Packet.FLAG_AUTO_ACK);
            message.send();
            logger.d("<<< send push message=%s", message);
            return ackRequestMgr.add(message.getSessionId(), context);
        }
        return null;
    }

    @Override
    public Future<HttpResponse> sendHttp(HttpRequest request) {
        if (connection.getSessionContext().handshakeOk()) {
            HttpRequestMessage message = new HttpRequestMessage(connection);
            message.method = request.getMethod();
            message.uri = request.getUri();
            message.headers = request.getHeaders();
            message.body = request.getBody();
            message.send();
            logger.d("<<< send http proxy, request=%s", request);
            return httpRequestMgr.add(message.getSessionId(), request);
        }
        return null;
    }

    @Override
    public void onSuccess(Packet response) {

    }

    @Override
    public void onTimeout(Packet request) {
        this.connection.reconnect();
    }
}
