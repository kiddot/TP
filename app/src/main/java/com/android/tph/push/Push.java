package com.android.tph.push;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.Nullable;

import com.android.tph.api.Client;
import com.android.tph.api.ClientListener;
import com.android.tph.api.Constants;
import com.android.tph.api.http.HttpRequest;
import com.android.tph.api.http.HttpResponse;
import com.android.tph.api.push.PushContext;
import com.android.tph.client.ClientConfig;
import com.android.tph.util.DefaultLogger;

import java.util.concurrent.Future;

/**
 * Created by kiddo on 17-7-11.
 */

public class Push {
    private static final String SP_FILE_NAME = "mpush.cfg";
    private static final String SP_KEY_CV = "clientVersion";
    private static final String SP_KEY_DI = "deviceId";
    private static final String SP_KEY_PK = "publicKey";
    private static final String SP_KEY_AS = "allotServer";
    private static final String SP_KEY_AT = "account";
    private static final String SP_KEY_TG = "tags";
    private static final String SP_KEY_LG = "log";
    public static Push I = I();
    private Context ctx;
    private ClientConfig clientConfig;
    private SharedPreferences sp;
    /*package*/ Client client;

    /**
     * 获取MPUSH实例
     *
     * @return
     */
    static /*package*/ Push I() {
        if (I == null) {
            synchronized (Push.class) {
                if (I == null) {
                    I = new Push();
                }
            }
        }
        return I;
    }

    /**
     * 初始化MPush, 使用之前必须先初始化
     *
     * @param context
     */
    public void init(Context context) {
        ctx = context.getApplicationContext();
        sp = ctx.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 是否已经初始化
     *
     * @return
     */
    public boolean hasInit() {
        return ctx != null;
    }

    /**
     * 是否已经初始化
     *
     * @return
     */
    public Push checkInit(Context context) {
        if (ctx == null) {
            init(context);
        }
        return this;
    }

    /**
     * MPushService 是否已启动
     *
     * @return
     */
    public boolean hasStarted() {
        return client != null;
    }

    /**
     * MPushClient 是否正在运行
     *
     * @return
     */
    public boolean hasRunning() {
        return client != null && client.isRunning();
    }

    /**
     * 设置client 配置项
     *
     * @param clientConfig
     */
    public void setClientConfig(ClientConfig clientConfig) {
        if (clientConfig.getPublicKey() == null
                || clientConfig.getAllotServer() == null
                || clientConfig.getClientVersion() == null) {
            throw new IllegalArgumentException("publicKey, allocServer can not be null");
        }

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SP_KEY_CV, clientConfig.getClientVersion())
                .putString(SP_KEY_DI, clientConfig.getDeviceId())
                .putString(SP_KEY_PK, clientConfig.getPublicKey())
                .putBoolean(SP_KEY_LG, clientConfig.isLogEnabled())
                .putString(SP_KEY_AS, clientConfig.getAllotServer());
        if (clientConfig.getUserId() != null) {
            editor.putString(SP_KEY_AT, clientConfig.getUserId());
        }

        if (clientConfig.getTags() != null) {
            editor.putString(SP_KEY_TG, clientConfig.getTags());
        }
        editor.apply();
        this.clientConfig = clientConfig;
    }

    /**
     * 启动推送服务
     */
    public void startPush() {
        if (hasInit()) {
            ctx.startService(new Intent(ctx, PushService.class));
        }
    }

    /**
     * 停止推送服务
     */
    public void stopPush() {
        if (hasInit()) {
            ctx.stopService(new Intent(ctx, PushService.class));
        }
    }

    /**
     * 暂停推送服务
     */
    public void pausePush() {
        if (hasStarted()) {
            client.stop();
        }
    }

    /**
     * 恢复推送服务
     */
    public void resumePush() {
        if (hasStarted()) {
            client.start();
        }
    }

    /**
     * 设置网络状态推送服务
     */
    public void onNetStateChange(boolean isConnected) {
        if (hasStarted()) {
            client.onNetStateChange(isConnected);
        }
    }

    /**
     * 绑定账号
     *
     * @param userId 要绑定的账号
     */
    public void bindAccount(String userId, String tags) {
        if (hasInit()) {
            sp.edit().putString(SP_KEY_AT, userId).apply();
            sp.edit().putString(SP_KEY_TG, tags).apply();
            if (hasStarted() && client.isRunning()) {
                client.bindUser(userId, tags);
            } else if (clientConfig != null) {
                clientConfig.setUserId(userId);
            }
        }
    }

    /**
     * 解绑账号
     */
    public void unbindAccount() {
        if (hasInit()) {
            sp.edit().remove(SP_KEY_AT).apply();
            if (hasStarted() && client.isRunning()) {
                client.unbindUser();
            } else {
                clientConfig.setUserId(null);
            }
        }
    }

    /**
     * 发送ACK
     *
     * @param messageId 要ACK的消息ID
     * @return
     */
    public boolean ack(int messageId) {
        if (hasStarted() && client.isRunning()) {
            client.ack(messageId);
            return true;
        }
        return false;
    }

    /**
     * 发送Push到服务端
     *
     * @param context Push上下文
     * @return
     */
    public Future<Boolean> sendPush(PushContext context) {
        if (hasStarted() && client.isRunning()) {
            return client.push(context);
        }
        return null;
    }

    /**
     * 发送Push到服务端, 不需要ACK
     *
     * @param content 要推送的数据
     * @return
     */
    public Future<Boolean> sendPush(byte[] content) {
        if (hasStarted() && client.isRunning()) {
            return client.push(PushContext.build(content));
        }
        return null;
    }

    /**
     * 发送Http代理请求
     *
     * @param request 要代理的http请求
     * @return
     */
    public Future<HttpResponse> sendHttpProxy(HttpRequest request) {
        if (hasStarted() && client.isRunning()) {
            return client.sendHttp(request);
        }
        return null;
    }

    public void enableLog(boolean enable) {
        if (clientConfig != null) {
            clientConfig.setLogEnabled(enable);
        }
    }

    @Nullable
    private ClientConfig getClientConfig() {
        if (clientConfig == null) {
            String clientVersion = sp.getString(SP_KEY_CV, null);
            String deviceId = sp.getString(SP_KEY_DI, null);
            String publicKey = sp.getString(SP_KEY_PK, null);
            String allocServer = sp.getString(SP_KEY_AS, null);
            boolean logEnabled = sp.getBoolean(SP_KEY_LG, false);
            clientConfig = ClientConfig.build()
                    .setPublicKey(publicKey)
                    .setAllotServer(allocServer)
                    .setDeviceId(deviceId)
                    .setOsName(Constants.DEF_OS_NAME)
                    .setOsVersion(Build.VERSION.RELEASE)
                    .setClientVersion(clientVersion)
                    .setLogger(new PushLog())
                    .setLogEnabled(logEnabled);
        }
        if (clientConfig.getClientVersion() == null
                || clientConfig.getPublicKey() == null
                || clientConfig.getAllotServer() == null) {
            return null;
        }

        if (clientConfig.getSessionStorageDir() == null) {
            clientConfig.setSessionStorage(new SPSessionStorage(sp));
        }

        if (clientConfig.getOsVersion() == null) {
            clientConfig.setOsVersion(Build.VERSION.RELEASE);
        }

        if (clientConfig.getUserId() == null) {
            clientConfig.setUserId(sp.getString(SP_KEY_AT, null));
        }

        if (clientConfig.getTags() == null) {
            clientConfig.setTags(sp.getString(SP_KEY_TG, null));
        }

        if (clientConfig.getLogger() instanceof DefaultLogger) {
            clientConfig.setLogger(new PushLog());
        }
        return clientConfig;
    }

    synchronized /*package*/ void create(ClientListener listener) {
        ClientConfig config = this.getClientConfig();
        if (config != null) {
            this.client = config.setClientListener(listener).create();
        }
    }

    synchronized /*package*/ void destroy() {
        if (client != null) {
            client.destroy();
        }
        I.client = null;
        I.clientConfig = null;
        I.sp = null;
        I.ctx = null;
    }
}
