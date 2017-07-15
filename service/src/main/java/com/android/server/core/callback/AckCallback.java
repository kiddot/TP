package com.android.server.core.callback;

import com.android.server.core.task.AckTask;

/**
 * Created by kiddo on 17-7-15.
 */

public interface AckCallback {
    void onSuccess(AckTask context);

    void onTimeout(AckTask context);
}
