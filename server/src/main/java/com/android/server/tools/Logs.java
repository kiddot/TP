package com.android.server.tools;

import com.android.server.tools.config.CC;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface Logs {
    boolean logInit = init();

    static boolean init() {
        if (logInit) return true;
        System.setProperty("log.home", CC.mp.log_dir);
        System.setProperty("log.root.level", CC.mp.log_level);
        LoggerFactory
                .getLogger("console")
                .info(CC.mp.cfg.root().render(ConfigRenderOptions.concise().setFormatted(true)));
        return true;
    }

    Logger Console = LoggerFactory.getLogger("console"),

    CONN = LoggerFactory.getLogger("mpush.conn.log"),

    MONITOR = LoggerFactory.getLogger("mpush.monitor.log"),

    PUSH = LoggerFactory.getLogger("mpush.push.log"),

    HB = LoggerFactory.getLogger("mpush.heartbeat.log"),

    CACHE = LoggerFactory.getLogger("mpush.cache.log"),

    RSD = LoggerFactory.getLogger("mpush.srd.log"),

    HTTP = LoggerFactory.getLogger("mpush.http.log"),

    PROFILE = LoggerFactory.getLogger("mpush.profile.log");
}
