package com.android.tph.demo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.tph.BuildConfig;
import com.android.tph.R;
import com.android.tph.api.Client;
import com.android.tph.api.ClientListener;
import com.android.tph.api.Constants;
import com.android.tph.api.http.HttpCallBack;
import com.android.tph.api.http.HttpMethod;
import com.android.tph.api.http.HttpRequest;
import com.android.tph.api.http.HttpResponse;
import com.android.tph.client.ClientConfig;
import com.android.tph.push.Notifications;
import com.android.tph.push.Push;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

/**
 * Created by kiddo on 17-7-11.
 */

public class MainActivity extends AppCompatActivity implements ClientListener{
    private static final String TAG = "MainActivity";
    private EditText mEtContent;
    private EditText mEtUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEtContent = (EditText) findViewById(R.id.main_content);
        mEtUserId = (EditText) findViewById(R.id.to);
        Notifications.I.init(this.getApplicationContext());
        Notifications.I.setSmallIcon(R.mipmap.ic_launcher);
        Notifications.I.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        SharedPreferences sp = this.getSharedPreferences("mpush.cfg", Context.MODE_PRIVATE);
        String alloc = sp.getString("allotServer", null);
        if (alloc != null) {
            EditText et = (EditText) findViewById(R.id.alloc);
            et.setText(alloc);
        }
    }

    /**
     * 在这里将用户输入的数据，做一层缓存
     * @param allocServer
     * @param userId
     */
    private void initPush(String allocServer, String userId) {
        //公钥由服务端提供和私钥对应
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB";

        String[] address = allocServer.split(":");
        String serverHost = address[1].substring(2, address[1].length());
        int serverPort = Integer.parseInt(address[2]);
        Toast.makeText(this, serverHost + serverPort, Toast.LENGTH_LONG).show();


        ClientConfig cc = ClientConfig.build()
                .setPublicKey(publicKey)
                .setAllotServer(allocServer)
                .setServerHost(serverHost)
                .setServerPort(serverPort)
                .setDeviceId(getDeviceId())
                .setClientVersion(BuildConfig.VERSION_NAME)
                .setLogger(new TPLog(this, (EditText) findViewById(R.id.log)))
                .setLogEnabled(BuildConfig.DEBUG)
                .setSessionStorageDir(MainActivity.class.getResource("/").getFile())
                .setEnableHttpProxy(true)
                .setUserId(userId);
        Push.I.checkInit(getApplicationContext()).setClientConfig(cc);
        ClientConfig.I.setClientListener(this);
    }

    private String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Activity.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            String time = Long.toString((System.currentTimeMillis() / (1000 * 60 * 60)));
            deviceId = time + time;
        }
        return deviceId;
    }

    public void bindUser(View btn) {
        EditText et = (EditText) findViewById(R.id.from);
        String userId = et.getText().toString().trim();
        if (!TextUtils.isEmpty(userId)) {
            Push.I.bindAccount(userId, "mpush:" + (int) (Math.random() * 10), "alias");
        }
    }

    public void startPush(View btn) {
        EditText et = (EditText) findViewById(R.id.alloc);
        String allocServer = et.getText().toString().trim();

        if (TextUtils.isEmpty(allocServer)) {
            Toast.makeText(this, "请填写正确的alloc地址", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!allocServer.startsWith("http://")) {
            allocServer = "http://" + allocServer;
        }


        EditText etUser = (EditText) findViewById(R.id.from);
        String userId = etUser.getText().toString().trim();

        initPush(allocServer, userId);

        Push.I.checkInit(this.getApplication()).startPush();
        Toast.makeText(this, "start push" + allocServer, Toast.LENGTH_SHORT).show();
    }

    public void sendPush(View btn) throws Exception {
        EditText et1 = (EditText) findViewById(R.id.alloc);
        String allocServer = et1.getText().toString().trim();

        if (TextUtils.isEmpty(allocServer)) {
            Toast.makeText(this, "请填写正确的alloc地址", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!allocServer.startsWith("http://")) {
            allocServer = "http://" + allocServer;
        }

        EditText toET = (EditText) findViewById(R.id.to);
        String to = toET.getText().toString().trim();

        EditText fromET = (EditText) findViewById(R.id.from);
        String from = fromET.getText().toString().trim();

        EditText helloET = (EditText) findViewById(R.id.httpProxy);
        String hello = helloET.getText().toString().trim();

        if (TextUtils.isEmpty(hello)) hello = "hello";

        JSONObject params = new JSONObject();
        params.put("userId", to);
        params.put("hello", from + " say:" + hello);

        final Context context = this.getApplicationContext();
        HttpRequest request = new HttpRequest(HttpMethod.POST, allocServer + "/push");
        byte[] body = params.toString().getBytes(Constants.UTF_8);
        request.setBody(body, "application/json; charset=utf-8");
        request.setTimeout((int) TimeUnit.SECONDS.toMillis(10));
        request.setCallback(new HttpCallBack() {
            @Override
            public void onResponse(final HttpResponse httpResponse) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (httpResponse.statusCode == 200) {
                            Toast.makeText(context, new String(httpResponse.body, Constants.UTF_8), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, httpResponse.reasonPhrase, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled() {

            }
        });
        Log.d(TAG, "sendPush: " + request.toString());
        Push.I.sendHttpProxy(request);
    }

    public void stopPush(View btn) {
        Push.I.stopPush();
        Toast.makeText(this, "stop push", Toast.LENGTH_SHORT).show();
    }

    public void pausePush(View btn) {
        Push.I.pausePush();
        Toast.makeText(this, "pause push", Toast.LENGTH_SHORT).show();
    }

    public void resumePush(View btn) {
        Push.I.resumePush();
        Toast.makeText(this, "resume push", Toast.LENGTH_SHORT).show();
    }

    public void unbindUser(View btn) {
        Push.I.unbindAccount();
        Toast.makeText(this, "unbind user", Toast.LENGTH_SHORT).show();
    }

    public void send(View view){
        String test = mEtContent.getText().toString();
        String userId = mEtUserId.getText().toString();
        byte[] content = test.getBytes(Constants.UTF_8);
        Push.I.sendPush(content, userId);
    }

    @Override
    public void onConnected(Client client) {

    }

    @Override
    public void onDisConnected(Client client) {

    }

    @Override
    public void onHandshakeOk(Client client, int heartbeat) {

    }

    @Override
    public void onReceivePush(Client client, byte[] content, int messageId) {
        try {
            String msg = new String(content, "UTF-8");
            Toast.makeText(this, msg , Toast.LENGTH_LONG).show();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
