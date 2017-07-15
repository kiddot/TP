package com.android.server.utils.security;

import com.android.server.core.Cipher;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.android.server.utils.security.AESUtils.KEY_ALGORITHM;

/**
 * Created by kiddo on 17-7-15.
 */

public class AesCipher implements Cipher {
    public final byte[] key;
    public final byte[] iv;
    private final IvParameterSpec zeroIv;
    private final SecretKeySpec keySpec;

    public AesCipher(byte[] key, byte[] iv) {
        this.key = key;
        this.iv = iv;
        this.zeroIv = new IvParameterSpec(iv);
        this.keySpec = new SecretKeySpec(key, KEY_ALGORITHM);
    }


    @Override
    public byte[] encrypt(byte[] data) {
        return AESUtils.encrypt(data, zeroIv, keySpec);
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return AESUtils.decrypt(data, zeroIv, keySpec);
    }

    @Override
    public String toString() {
        return toString(key) + ',' + toString(iv);
    }

    public String toString(byte[] a) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i != 0) b.append('|');
            b.append(a[i]);
        }
        return b.toString();
    }

    public static byte[] toArray(String str) {
        String[] a = str.split("\\|");
        if (a.length != CipherBox.I.getAesKeyLength()) {
            return null;
        }
        byte[] bytes = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            bytes[i] = Byte.parseByte(a[i]);
        }
        return bytes;
    }
}
