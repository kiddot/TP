package com.android.server.config;

import com.android.server.utils.Utils;

/**
 * Created by kiddo on 17-7-15.
 */

public class ConfigManager {
    public static final ConfigManager I = new ConfigManager();

    private ConfigManager() {
    }

    public int getHeartbeat(int min, int max) {
        return Math.max(
                ConfigCenter.min_heartbeat,
                Math.min(max,ConfigCenter.max_heartbeat)
        );
    }

    /**
     * 获取内网IP地址
     *
     * @return 内网IP地址
     */
    public String getLocalIp() {
        return Utils.getLocalIp();
    }

    /**
     * 获取外网IP地址
     *
     * @return 外网IP地址
     */
    public String getPublicIp() {

        String localIp = Utils.getLocalIp();

        String remoteIp = null;

        if (remoteIp == null) {
            remoteIp = Utils.getExtranetIp();
        }

        return remoteIp == null ? localIp : remoteIp;
    }
}
