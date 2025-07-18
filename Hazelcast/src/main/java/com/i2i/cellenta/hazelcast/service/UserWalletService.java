package com.i2i.cellenta.hazelcast.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.i2i.cellenta.hazelcast.config.HazelcastConnector;
import com.i2i.cellenta.hazelcast.model.UserQuota;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserWalletService {
    private static final HazelcastInstance hz = HazelcastConnector.getInstance();
    private static final String MAP_NAME = "userWallets";


    public static void createWallet(String msisdn, Integer balance) {
        IMap<String, Integer> userBalances = hz.getMap(MAP_NAME);
        if (msisdn == null || balance == null) {
            throw new IllegalArgumentException("MSISDN or balance cannot be null.");
        }

        if (userBalances.containsKey(msisdn)) {
            throw new IllegalArgumentException("Balance for MSISDN " + msisdn
                    + " already exists. Use the update method");
        }

        userBalances.put(msisdn, balance);
    }

    public static Integer getWalletAmount(String msisdn) {
        IMap<String, Integer> userBalances = hz.getMap(MAP_NAME);

        if (msisdn == null) {
            throw new IllegalArgumentException("MSISDN cannot be null.");
        }
        if (!userBalances.containsKey(msisdn)) {
            throw new IllegalArgumentException("MSISDN '" + msisdn + "' not found.");
        }

        return userBalances.get(msisdn);
    }

    public static boolean updateWalletAmountBy(String msisdn, Integer  value) {
        IMap<String, Integer> userBalances = hz.getMap(MAP_NAME);
        if (msisdn == null || value == null) {
            throw new IllegalArgumentException("MSISDN or balance cannot be null.");
        }
        if (!userBalances.containsKey(msisdn)) {
            throw new IllegalArgumentException("MSISDN '" + msisdn + "' not found.");
        }

        userBalances.computeIfPresent(msisdn, (k, v) -> v + value);
        return true;
    }

    public static int size() {
        IMap<String, String> registeredUsers = hz.getMap(MAP_NAME);

        if (registeredUsers == null) {
            throw new IllegalArgumentException("Hazelcast map 'userWallets' is null.");
        }

        return registeredUsers.size();
    }

    public static boolean removeWallet(String msisdn) {
        IMap<String, Integer> userBalances = hz.getMap(MAP_NAME);

        if (!userBalances.containsKey(msisdn)) {
            throw new IllegalArgumentException("MSISDN '" + msisdn + "' does not exist.");
        }

        Integer removed = userBalances.remove(msisdn);

        if (removed == null) {
            throw new IllegalArgumentException("Removal failed for MSISDN '" + msisdn + "'.");
        }
        System.out.println(msisdn + " was removed successfully ");
        return true;
    }

    public static boolean removeAllWallets() {
        IMap<String, Integer> userBalances = hz.getMap(MAP_NAME);

        if (userBalances.isEmpty()) {
            throw new IllegalArgumentException("The map is empty. Nothing to remove.");
        }

        userBalances.clear();
        System.out.println("All user balances removed successfully.");
        return true;
    }

    public static List<Map.Entry<String, Integer>> getAllWallets() {
        IMap<String, Integer> userBalances = hz.getMap(MAP_NAME);

        if (userBalances == null) {
            throw new IllegalArgumentException("Hazelcast map 'userWallets' is null.");
        }

        Set<Map.Entry<String, Integer>> entries = userBalances.entrySet();

        if (entries.isEmpty()) {
            throw new IllegalArgumentException("No active balances found in 'userWallets' map.");
        }

        return new ArrayList<>(entries);
    }
}




