package com.android.server.core.connection;

import io.netty.channel.Channel;

/**
 * Created by kiddo on 17-7-15.
 */

public interface ConnectionManager {
    Connection get(Channel channel);

    Connection removeAndClose(Channel channel);

    void add(Connection connection);

    int getConnNum();

    void init();

    void destroy();
}
