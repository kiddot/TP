package com.android.tph.api.http;

/**
 * Created by kiddo on 17-7-11.
 */

public interface HttpCallBack {
    void onResponse(HttpResponse response);

    void onCancelled();
}
