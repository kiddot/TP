package com.android.tph.demo;

import android.app.Activity;
import android.util.Log;
import android.widget.EditText;

import com.android.tph.api.Constants;
import com.android.tph.api.Logger;
import com.android.tph.push.PushLog;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * Created by kiddo on 17-7-11.
 */

public class TPLog implements Logger {
    private EditText logView;
    private Activity activity;

    private PushLog mPushLog;

    public TPLog(Activity activity, EditText logView) {
        this.activity = activity;
        this.logView = logView;
        this.mPushLog = new PushLog();
    }

    @Override
    public void enable(boolean b) {
        this.mPushLog.enable(true);
    }

    @Override
    public void d(String s, Object... objects) {
        mPushLog.d(s, objects);
        //Log.d("push", s + Arrays.toString(objects));
        append(null, s, objects);
    }

    @Override
    public void i(String s, Object... objects) {
        mPushLog.i(s, objects);
        append(null, s, objects);
    }

    @Override
    public void w(String s, Object... objects) {
        mPushLog.w(s, objects);
        append(null, s, objects);
    }

    @Override
    public void e(Throwable throwable, String s, Object... objects) {
        mPushLog.e(throwable, s, objects);
        append(throwable, s, objects);
    }

    private void append(final Throwable throwable, final String s, final Object... objects) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logView.getText().append(String.format(s, objects)).append('\n').append('\n');
                if (throwable != null) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    throwable.printStackTrace(new PrintStream(buffer));
                    logView.getText().append(new String(buffer.toByteArray(), Constants.UTF_8));
                }
            }
        });
    }
}
