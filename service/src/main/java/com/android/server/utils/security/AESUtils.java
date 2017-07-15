package com.android.server.utils.security;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by kiddo on 17-7-15.
 */

public class AESUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(AESUtils.class);
    public static final String KEY_ALGORITHM = "AES";
    public static final String KEY_ALGORITHM_PADDING = "AES/CBC/PKCS5Padding";


    public static SecretKey getSecretKey(byte[] seed) throws Exception {
        SecureRandom secureRandom = new SecureRandom(seed);
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGenerator.init(secureRandom);
        return keyGenerator.generateKey();
    }

    public static byte[] encrypt(byte[] data, byte[] encryptKey, byte[] iv) {
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(encryptKey, KEY_ALGORITHM);
        return encrypt(data, zeroIv, key);
    }

    public static byte[] decrypt(byte[] data, byte[] decryptKey, byte[] iv) {
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(decryptKey, KEY_ALGORITHM);

        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        try {
            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(byte[] data, IvParameterSpec zeroIv, SecretKeySpec keySpec) {
        try {
            //Profiler.enter("time cost on [aes encrypt]: data length=" + data.length);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, zeroIv);
            return cipher.doFinal(data);
        } catch (Exception e) {
//            LOGGER.error("AES encrypt ex, iv={}, key={}",
//                    Arrays.toString(zeroIv.getIV()),
//                    Arrays.toString(keySpec.getEncoded()), e);
//            throw new CryptoException("AES encrypt ex", e);
        } finally {
//            Profiler.release();
        }
        return null;
    }

    public static byte[] decrypt(byte[] data, IvParameterSpec zeroIv, SecretKeySpec keySpec) {
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, zeroIv);
            return cipher.doFinal(data);
        } catch (Exception e) {
        } finally {
        }
        return null;
    }
}
