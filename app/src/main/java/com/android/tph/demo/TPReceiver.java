package com.android.tph.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.tph.api.Constants;
import com.android.tph.push.Notifications;
import com.android.tph.push.Push;
import com.android.tph.push.PushService;

import org.json.JSONObject;

/**
 * 用户对推送事件的自定义接受处理
 * Created by kiddo on 17-7-11.
 */

public class TPReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PushService.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            byte[] bytes = intent.getByteArrayExtra(PushService.EXTRA_PUSH_MESSAGE);
            int messageId = intent.getIntExtra(PushService.EXTRA_PUSH_MESSAGE_ID, 0);
            String message = new String(bytes, Constants.UTF_8);

            Toast.makeText(context, "收到新的通知：" + message, Toast.LENGTH_SHORT).show();

            if (messageId > 0) Push.I.ack(messageId);

            if (TextUtils.isEmpty(message)) return;

            NotificationDo ndo = fromJson(message);

            if (ndo != null) {
                Intent it = new Intent(context, TPReceiver.class);
                it.setAction(PushService.ACTION_NOTIFICATION_OPENED);
                if (ndo.getExtras() != null) it.putExtra("my_extra", ndo.getExtras().toString());
                if (TextUtils.isEmpty(ndo.getTitle())) ndo.setTitle("MPush");
                if (TextUtils.isEmpty(ndo.getTicker())) ndo.setTicker(ndo.getTitle());
                if (TextUtils.isEmpty(ndo.getContent())) ndo.setContent(ndo.getTitle());
                ndo.setNid(messageId);//每条信息的id生成一个通知栏通知
                Notifications.I.notify(ndo, it);
            }
        } else if (PushService.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Notifications.I.clean(intent);
            String extras = intent.getStringExtra("my_extra");
            Toast.makeText(context, "通知被点击了， extras=" + extras, Toast.LENGTH_SHORT).show();
        } else if (PushService.ACTION_KICK_USER.equals(intent.getAction())) {
            Toast.makeText(context, "用户被踢下线了", Toast.LENGTH_SHORT).show();
        } else if (PushService.ACTION_BIND_USER.equals(intent.getAction())) {
            Toast.makeText(context, "绑定用户:"
                            + intent.getStringExtra(PushService.EXTRA_USER_ID)
                            + (intent.getBooleanExtra(PushService.EXTRA_BIND_RET, false) ? "成功" : "失败")
                    , Toast.LENGTH_SHORT).show();
        } else if (PushService.ACTION_UNBIND_USER.equals(intent.getAction())) {
            Toast.makeText(context, "解绑用户:"
                            + (intent.getBooleanExtra(PushService.EXTRA_BIND_RET, false)
                            ? "成功"
                            : "失败")
                    , Toast.LENGTH_SHORT).show();
        } else if (PushService.ACTION_CONNECTIVITY_CHANGE.equals(intent.getAction())) {
            Toast.makeText(context, intent.getBooleanExtra(PushService.EXTRA_CONNECT_STATE, false)
                            ? "MPUSH连接建立成功"
                            : "MPUSH连接断开"
                    , Toast.LENGTH_SHORT).show();
        } else if (PushService.ACTION_HANDSHAKE_OK.equals(intent.getAction())) {
            Toast.makeText(context, "MPUSH握手成功, 心跳:" + intent.getIntExtra(PushService.EXTRA_HEARTBEAT, 0)
                    , Toast.LENGTH_SHORT).show();
        }
    }

    private NotificationDo fromJson(String message) {
        try {
            JSONObject messageDO = new JSONObject(message);
            if (messageDO != null) {
                JSONObject jo = new JSONObject(messageDO.optString("content"));
                NotificationDo ndo = new NotificationDo();
                ndo.setContent(jo.optString("content"));
                ndo.setTitle(jo.optString("title"));
                ndo.setTicker(jo.optString("ticker"));
                ndo.setNid(jo.optInt("nid", 1));
                ndo.setExtras(jo.optJSONObject("extras"));
                return ndo;
            }
        } catch (Exception e) {
            NotificationDo ndo = new NotificationDo();
            ndo.setContent(message);
            ndo.setTitle(message);
            ndo.setTicker(message);
            return ndo;
        }
        return null;
    }
}
