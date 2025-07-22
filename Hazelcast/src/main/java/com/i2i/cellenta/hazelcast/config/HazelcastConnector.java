package com.i2i.cellenta.hazelcast.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastConnector {
    private static final HazelcastInstance hazelcastInstance;

    static {
        // Initialize once at class load time using external config class
        ClientConfig config = HazelcastClientConfig.getConfig();
        hazelcastInstance = HazelcastClient.newHazelcastClient(config);
    }

    /**
     * Provides access to the singleton Hazelcast client instance.
     * returns a shared HazelcastInstance
     */
    public static HazelcastInstance getInstance() {
        return hazelcastInstance;
    }

    public static void shutdown() {
        if (hazelcastInstance != null) {
            hazelcastInstance.shutdown();
            System.out.println("Hazelcast client shut down successfully.");
        }
    }
}


