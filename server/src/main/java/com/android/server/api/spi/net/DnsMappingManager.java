package com.android.server.api.spi.net;


import com.android.server.api.service.Service;
import com.android.server.api.spi.SpiLoader;

public interface DnsMappingManager extends Service {

//    static DnsMappingManager create() {
//        return SpiLoader.load(DnsMappingManager.class);
//    }

    DnsMapping lookup(String origin);
}
