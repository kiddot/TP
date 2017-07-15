package com.android.server.core;

/**
 * Created by kiddo on 17-7-15.
 */

public interface Cipher {
    byte[] decrypt(byte[] data);

    byte[] encrypt(byte[] data);
}
