package com.i2i.cellenta.hazelcast.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.i2i.cellenta.hazelcast.config.HazelcastConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserWithPassService {
    private static final HazelcastInstance hz = HazelcastConnector.getInstance();
    private static final String MAP_NAME = "registeredUsers";


    public static void registerUser(String msisdn, String Password) {
        IMap<String, String> registeredUsers = hz.getMap(MAP_NAME);
        if (msisdn == null || Password == null) {
            throw new IllegalArgumentException("MSISDN or Password cannot be null.");
        }

        if (registeredUsers.containsKey(msisdn)) {
            throw new IllegalArgumentException(msisdn + " already exists. Use the update method");
        }

        registeredUsers.put(msisdn, Password);
    }

    public static String getRegisteredUser(String msisdn) {
        IMap<String, String> registeredUsers = hz.getMap(MAP_NAME);

        if (msisdn == null) {
            throw new IllegalArgumentException("MSISDN cannot be null.");
        }
        if (!registeredUsers.containsKey(msisdn)) {
            throw new IllegalArgumentException("MSISDN '" + msisdn + "' not found.");
        }

        return registeredUsers.get(msisdn);
    }

    public static int size() {
        IMap<String, String> registeredUsers = hz.getMap(MAP_NAME);

        if (registeredUsers == null) {
            throw new IllegalArgumentException("Hazelcast map 'registeredUsers' is null.");
        }

        return registeredUsers.size();
    }

    public static boolean unregisterUser(String msisdn) {
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

    public static boolean unregisterAllUsers() {
        IMap<String, String> registeredUsers = hz.getMap(MAP_NAME);

        if (registeredUsers.isEmpty()) {
            throw new IllegalArgumentException("The map is empty. Nothing to remove.");
        }

        registeredUsers.clear();
        System.out.println("All users are removed successfully.");
        return true;
    }

    public static List<String> getAllRegisteredUsers() {
        IMap<String, String> registeredUsers = hz.getMap(MAP_NAME);

        if (registeredUsers == null) {
            throw new IllegalArgumentException("Hazelcast map 'registeredUsers' is null.");
        }

        Set<String> keys = registeredUsers.keySet();

        if (keys.isEmpty()) {
            throw new IllegalArgumentException("No active balances found in 'registeredUsers' map.");
        }

        return new ArrayList<>(keys);
    }
}

