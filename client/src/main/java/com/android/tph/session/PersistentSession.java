package com.android.tph.session;


import com.android.tph.api.connection.Cipher;
import com.android.tph.security.AesCipher;
import com.android.tph.util.Strings;

public final class PersistentSession {
    public String sessionId;
    public long expireTime;
    public Cipher cipher;

    public boolean isExpired() {
        return expireTime < System.currentTimeMillis();
    }

    public static String encode(PersistentSession session) {
        return session.sessionId
                + "," + session.expireTime
                + "," + session.cipher.toString();
    }

    public static PersistentSession decode(String value) {
        String[] array = value.split(",");
        if (array.length != 4) return null;
        PersistentSession session = new PersistentSession();
        session.sessionId = array[0];
        session.expireTime = Strings.toLong(array[1], 0);
        byte[] key = AesCipher.toArray(array[2]);
        byte[] iv = AesCipher.toArray(array[3]);
        if (key == null || iv == null) return null;
        session.cipher = new AesCipher(key, iv);
        return session;
    }

    @Override
    public String toString() {
        return "PersistentSession{" +
                "sessionId='" + sessionId + '\'' +
                ", expireTime=" + expireTime +
                ", cipher=" + cipher +
                '}';
    }
}
