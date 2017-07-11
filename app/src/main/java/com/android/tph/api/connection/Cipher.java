package com.android.tph.api.connection;

/**
 * Created by kiddo on 17-7-11.
 */

public interface Cipher {
    byte[] decrypt(byte[] data);

    byte[] encrypt(byte[] data);
}
