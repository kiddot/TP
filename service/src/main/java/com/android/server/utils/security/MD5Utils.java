package com.android.server.utils.security;

import com.android.server.common.Constants;
import com.android.server.utils.IOUtils;
import com.android.server.utils.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by kiddo on 17-7-15.
 */

public class MD5Utils {
    public static String encrypt(File file) {
        InputStream in = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            byte[] buffer = new byte[1024];//10k
            int readLen;
            while ((readLen = in.read(buffer)) != -1) {
                digest.update(buffer, 0, readLen);
            }
            return toHex(digest.digest());
        } catch (Exception e) {
            return Strings.EMPTY;
        } finally {
            IOUtils.close(in);
        }
    }


    public static String encrypt(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(text.getBytes("UTF-8"));
            return toHex(digest.digest());
        } catch (Exception e) {
            return Strings.EMPTY;
        }
    }

    public static String encrypt(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(bytes);
            return toHex(digest.digest());
        } catch (Exception e) {
            return Strings.EMPTY;
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder buffer = new StringBuilder(bytes.length * 2);

        for (int i = 0; i < bytes.length; ++i) {
            buffer.append(Character.forDigit((bytes[i] & 240) >> 4, 16));
            buffer.append(Character.forDigit(bytes[i] & 15, 16));
        }

        return buffer.toString();
    }

    public static String hmacSha1(String data, String encryptKey) {
        final String HMAC_SHA1 = "HmacSHA1";
        try {
            SecretKeySpec signingKey = new SecretKeySpec(encryptKey.getBytes("UTF-8"), HMAC_SHA1);
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(signingKey);
            mac.update(data.getBytes("UTF-8"));
            return toHex(mac.doFinal());
        } catch (Exception e) {
            return Strings.EMPTY;
        }
    }


    public static String sha1(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return toHex(digest.digest(data.getBytes("UTF-8")));
        } catch (Exception e) {
            return Strings.EMPTY;
        }
    }
}
