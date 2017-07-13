package com.android.server.api;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
public interface Constants {
    @android.annotation.SuppressLint("NewApi")
    Charset UTF_8 = StandardCharsets.UTF_8;
    byte[] EMPTY_BYTES = new byte[0];
    String HTTP_HEAD_READ_TIMEOUT = "readTimeout";
    String EMPTY_STRING = "";
    String ANY_HOST = "0.0.0.0";
}
