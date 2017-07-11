package com.android.tph.api.http;

import com.android.tph.api.Constants;

import java.util.Map;

/**
 * Created by kiddo on 17-7-11.
 */

public final class HttpResponse {
    public final int statusCode;
    public final String reasonPhrase;
    public final Map<String, String> headers;
    public final byte[] body;

    public HttpResponse(int statusCode, String reasonPhrase, Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                ", reasonPhrase='" + reasonPhrase + '\'' +
                ", headers=" + headers +
                ", body=" + (body == null ? "" : new String(body, Constants.UTF_8)) +
                '}';
    }
}
