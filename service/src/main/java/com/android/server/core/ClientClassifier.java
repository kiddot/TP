package com.android.server.core;

/**
 * Created by kiddo on 17-7-15.
 */

public interface ClientClassifier {
    //ClientClassifier I = ClientClassifierFactory.create();

    int getClientType(String osName);
}
