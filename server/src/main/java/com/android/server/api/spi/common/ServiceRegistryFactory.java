package com.android.server.api.spi.common;

import com.android.server.api.spi.Factory;
import com.android.server.api.spi.SpiLoader;

import javax.imageio.spi.ServiceRegistry;

public interface ServiceRegistryFactory extends Factory<ServiceRegistry> {
    static ServiceRegistry create() {
        return SpiLoader.load(ServiceRegistryFactory.class).get();
    }
}
