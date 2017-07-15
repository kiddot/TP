package com.android.server.common;

import java.nio.charset.Charset;

/**
 * Created by kiddo on 17-7-15.
 */

public interface Constants {
    Charset UTF_8 = Charset.forName("UTF-8");
    byte[] EMPTY_BYTES = new byte[0];
    String HTTP_HEAD_READ_TIMEOUT = "readTimeout";
    String EMPTY_STRING = "";
    String ANY_HOST = "0.0.0.0";
}
