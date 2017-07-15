package com.android.server.boot;

import org.apache.log4j.PropertyConfigurator;

/**
 * Created by kiddo on 17-7-15.
 */

public class Main {

    public static void main(String[] args) {
        PropertyConfigurator.configure("log4j.properties");
        ServerLauncher launcher = new ServerLauncher();
        launcher.start();
    }
}
