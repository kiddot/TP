package com.android.server.api.spi.push;


import com.android.server.api.spi.Factory;
import com.android.server.api.spi.SpiLoader;

public interface MessagePusherFactory extends Factory<MessagePusher> {

    static MessagePusher create() {
        return SpiLoader.load(MessagePusherFactory.class).get();
    }
}
