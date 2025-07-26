package com.i2i.cellenta.hazelcast.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.i2i.cellenta.hazelcast.config.HazelcastConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MsisdnService {
    private static final HazelcastInstance hz = HazelcastConnector.getInstance();
    private static final String MAP_NAME = "registeredMsisdn";
    private static final Random RANDOM = new Random();


    public static void registerMsisdn(String msisdn) {
        IMap<String, String> registeredUsers = hz.getMap(MAP_NAME);
        if (msisdn == null) {
            throw new IllegalArgumentException("MSISDN cannot be null.");
        }
        if (msisdn.startsWith("\"") && msisdn.endsWith("\"")) {
            msisdn = msisdn.substring(1, msisdn.length() - 1);
        }
        msisdn = msisdn.replace("\"", "");
        if (registeredUsers.containsKey(msisdn)) {
            throw new IllegalArgumentException(msisdn + " already exists. Use the update method");
        }

        registeredUsers.put(msisdn, msisdn);
    }

    public static int size() {
        IMap<String, String> registeredUsers = hz.getMap(MAP_NAME);

        if (registeredUsers == null) {
            throw new IllegalArgumentException("Hazelcast map 'registeredMsisdn' is null.");
        }

        return registeredUsers.size();
    }


    public static boolean unregisterMsisdn(String msisdn) {
        IMap<String, String> registeredUsers = hz.getMap(MAP_NAME);

        if (!registeredUsers.containsKey(msisdn)) {
            throw new IllegalArgumentException("MSISDN '" + msisdn + "' does not exist.");
        }

        String removed = registeredUsers.remove(msisdn);

        if (removed == null) {
            throw new IllegalArgumentException("Removal failed for MSISDN '" + msisdn + "'.");
        }
        System.out.println(msisdn + " was removed successfully ");
        return true;
    }

    public static boolean unregisterAllMsisdns() {
        IMap<String, String> registeredUsers = hz.getMap(MAP_NAME);

        if (registeredUsers.isEmpty()) {
            throw new IllegalArgumentException("The map is empty. Nothing to remove.");
        }

        registeredUsers.clear();
        System.out.println("All users are removed successfully.");
        return true;
    }

    public static List<String> getAllRegisteredMsisdns() {
        IMap<String, String> registeredUsers = hz.getMap(MAP_NAME);

        if (registeredUsers == null) {
            throw new IllegalArgumentException("Hazelcast map 'registeredMsisdn' is null.");
        }

        Set<String> keys = registeredUsers.keySet();

        if (keys.isEmpty()) {
            throw new IllegalArgumentException("No active balances found in 'registeredMsisdn' map.");
        }

        return new ArrayList<>(keys);
    }
}

