package com.android.tph.push;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.tph.BuildConfig;
import com.android.tph.api.Client;
import com.android.tph.api.ClientListener;


/**
 * Created by kiddo on 17-7-11.
 */

public class PushService extends Service implements ClientListener{
    public static final String ACTION_MESSAGE_RECEIVED = "com.mpush.MESSAGE_RECEIVED";
    public static final String ACTION_NOTIFICATION_OPENED = "com.mpush.NOTIFICATION_OPENED";
    public static final String ACTION_KICK_USER = "com.mpush.KICK_USER";
    public static final String ACTION_CONNECTIVITY_CHANGE = "com.mpush.CONNECTIVITY_CHANGE";
    public static final String ACTION_HANDSHAKE_OK = "com.mpush.HANDSHAKE_OK";
    public static final String ACTION_BIND_USER = "com.mpush.BIND_USER";
    public static final String ACTION_UNBIND_USER = "com.mpush.UNBIND_USER";
    public static final String EXTRA_PUSH_MESSAGE = "push_message";
    public static final String EXTRA_PUSH_MESSAGE_ID = "push_message_id";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_DEVICE_ID = "device_id";
    public static final String EXTRA_BIND_RET = "bind_ret";
    public static final String EXTRA_CONNECT_STATE = "connect_state";
    public static final String EXTRA_HEARTBEAT = "heartbeat";
    private int SERVICE_START_DELAYED = 5;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        cancelAutoStartService(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!Push.I.hasStarted()) {
            Push.I.checkInit(this).create(this);
        }
        if (Push.I.hasStarted()) {
            if (PushReceiver.hasNetwork(this)) {
                Push.I.client.start();
            }
            PushFakeService.startForeground(this);
            // TODO : flags = START_STICKY;
            SERVICE_START_DELAYED = 5;
            return super.onStartCommand(intent, flags, startId);
        } else {
            int ret = super.onStartCommand(intent, flags, startId);
            stopSelf();
            SERVICE_START_DELAYED += SERVICE_START_DELAYED;
            return ret;
        }
    }

    /**
     * service停掉后自动启动应用
     *
     * @param context
     * @param delayed 延后启动的时间，单位为秒
     */
    private static void startServiceAfterClosed(Context context, int delayed) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayed * 1000, getOperation(context));
    }

    public static void cancelAutoStartService(Context context) {
        AlarmManager alarm = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(getOperation(context));
    }

    private static PendingIntent getOperation(Context context) {
        Intent intent = new Intent(context, PushService.class);
        PendingIntent operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return operation;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        PushReceiver.cancelAlarm(this);
        Push.I.destroy();
        startServiceAfterClosed(this, SERVICE_START_DELAYED);//5s后重启
    }

    @Override
    public void onReceivePush(Client client, byte[] content, int messageId) {
        sendBroadcast(new Intent(ACTION_MESSAGE_RECEIVED)
                .addCategory(BuildConfig.APPLICATION_ID)
                .putExtra(EXTRA_PUSH_MESSAGE, content)
                .putExtra(EXTRA_PUSH_MESSAGE_ID, messageId)
        );
    }

    @Override
    public void onKickUser(String deviceId, String userId) {
        Push.I.unbindAccount();
        sendBroadcast(new Intent(ACTION_KICK_USER)
                .addCategory(BuildConfig.APPLICATION_ID)
                .putExtra(EXTRA_DEVICE_ID, deviceId)
                .putExtra(EXTRA_USER_ID, userId)
        );
    }

    @Override
    public void onBind(boolean success, String userId) {
        sendBroadcast(new Intent(ACTION_BIND_USER)
                .addCategory(BuildConfig.APPLICATION_ID)
                .putExtra(EXTRA_BIND_RET, success)
                .putExtra(EXTRA_USER_ID, userId)
        );
    }

    @Override
    public void onUnbind(boolean success, String userId) {
        sendBroadcast(new Intent(ACTION_UNBIND_USER)
                .addCategory(BuildConfig.APPLICATION_ID)
                .putExtra(EXTRA_BIND_RET, success)
                .putExtra(EXTRA_USER_ID, userId)
        );
    }

    @Override
    public void onConnected(Client client) {
        sendBroadcast(new Intent(ACTION_CONNECTIVITY_CHANGE)
                .addCategory(BuildConfig.APPLICATION_ID)
                .putExtra(EXTRA_CONNECT_STATE, true)
        );
    }

    @Override
    public void onDisConnected(Client client) {
        PushReceiver.cancelAlarm(this);
        sendBroadcast(new Intent(ACTION_CONNECTIVITY_CHANGE)
                .addCategory(BuildConfig.APPLICATION_ID)
                .putExtra(EXTRA_CONNECT_STATE, false)
        );
    }

    @Override
    public void onHandshakeOk(Client client, int heartbeat) {
        PushReceiver.startAlarm(this, heartbeat - 1000);
        sendBroadcast(new Intent(ACTION_HANDSHAKE_OK)
                .addCategory(BuildConfig.APPLICATION_ID)
                .putExtra(EXTRA_HEARTBEAT, heartbeat)
        );
    }
}
