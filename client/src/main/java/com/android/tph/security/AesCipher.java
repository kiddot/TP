package com.android.tph.security;


import com.android.tph.api.connection.Cipher;
import com.android.tph.util.crypto.AESUtils;

public final class AesCipher implements Cipher {
    public final byte[] key;
    public final byte[] iv;

    public AesCipher(byte[] key, byte[] iv) {
        this.key = key;
        this.iv = iv;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return AESUtils.decrypt(data, key, iv);
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return AESUtils.encrypt(data, key, iv);
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
        if (a.length != CipherBox.INSTANCE.getAesKeyLength()) {
            return null;
        }
        byte[] bytes = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            bytes[i] = Byte.parseByte(a[i]);
        }
        return bytes;
    }
}
