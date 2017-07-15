package com.android.server.core.connection;

import com.android.server.config.ConfigCenter;
import com.android.server.core.CacheManager;
import com.android.server.core.cache.CacheKeys;
import com.android.server.utils.Strings;
import com.android.server.utils.security.MD5Utils;

/**
 * Created by kiddo on 17-7-15.
 */

public class ReusableSessionManager {
    public static final ReusableSessionManager I = new ReusableSessionManager();
    private final int expiredTime = ConfigCenter.session_expired_time;
    private final CacheManager cacheManager = null;//TODO

    public boolean cacheSession(ReusableSession session) {
        String key = CacheKeys.getSessionKey(session.sessionId);
        cacheManager.set(key, ReusableSession.encode(session.context), expiredTime);
        return true;
    }

    public ReusableSession querySession(String sessionId) {
        String key = CacheKeys.getSessionKey(sessionId);
        String value = cacheManager.get(key, String.class);
        if (Strings.isBlank(value)) return null;
        return ReusableSession.decode(value);
    }

    public ReusableSession genSession(SessionContext context) {
        long now = System.currentTimeMillis();
        ReusableSession session = new ReusableSession();
        session.context = context;
        session.sessionId = MD5Utils.encrypt(context.deviceId + now);
        session.expireTime = now + expiredTime * 1000;
        return session;
    }
}
