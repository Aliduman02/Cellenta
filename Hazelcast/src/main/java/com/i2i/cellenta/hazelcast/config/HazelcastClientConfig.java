package com.i2i.cellenta.hazelcast.config;

import com.hazelcast.client.config.ClientConfig;

public class HazelcastClientConfig  {
    public static ClientConfig getConfig() {
        ClientConfig config = new ClientConfig();
        config.setProperty("hazelcast.logging.type", "slf4j");
        config.getNetworkConfig().addAddress("104.198.77.152:5701");
        return config;
    }
}
