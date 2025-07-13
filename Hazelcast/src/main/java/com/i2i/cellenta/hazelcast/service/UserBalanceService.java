package com.i2i.cellenta.hazelcast.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.i2i.cellenta.hazelcast.config.HazelcastConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserBalanceService {
    private static final HazelcastInstance hz = HazelcastConnector.getInstance();
    private static final String MAP_NAME = "userBalances";


    public static void createUserBalance(String msisdn, Double balance) {
        IMap<String, Double> userBalances = hz.getMap(MAP_NAME);
        if (msisdn == null || balance == null) {
            throw new IllegalArgumentException("MSISDN or balance cannot be null.");
        }

        if (userBalances.containsKey(msisdn)) {
            throw new IllegalStateException("Balance for MSISDN " + msisdn
                    + " already exists. Use the update method");
        }

        userBalances.put(msisdn, balance);
    }

    public static double getUserBalance(String msisdn) {
        IMap<String, Double> userBalances = hz.getMap(MAP_NAME);

        if (msisdn == null) {
            throw new IllegalArgumentException("MSISDN cannot be null.");
        }
        if (!userBalances.containsKey(msisdn)) {
            throw new IllegalArgumentException("MSISDN '" + msisdn + "' not found.");
        }

        return userBalances.get(msisdn);
    }

    public static boolean updateUserBalanceBy(String msisdn, Double  value) {
        IMap<String, Double> userBalances = hz.getMap(MAP_NAME);
        if (msisdn == null || value == null) {
            throw new IllegalArgumentException("MSISDN or balance cannot be null.");
        }
        if (!userBalances.containsKey(msisdn)) {
            throw new IllegalArgumentException("MSISDN '" + msisdn + "' not found.");
        }

        userBalances.computeIfPresent(msisdn, (k, v) -> v + value);
        return true;
    }

    public static boolean removeUserBalance(String msisdn) {
        IMap<String, Double> userBalances = hz.getMap(MAP_NAME);

        if (!userBalances.containsKey(msisdn)) {
            throw new IllegalArgumentException("MSISDN '" + msisdn + "' does not exist.");
        }

        Double removed = userBalances.remove(msisdn);

        if (removed == null) {
            throw new IllegalStateException("Removal failed for MSISDN '" + msisdn + "'.");
        }
        System.out.println(msisdn + " was removed successfully ");
        return true;
    }

    public static List<Map.Entry<String, Double>> getAllUserBalances() {
        IMap<String, Double> userBalances = hz.getMap(MAP_NAME);

        if (userBalances == null) {
            throw new IllegalStateException("Hazelcast map 'userBalances' is null.");
        }

        Set<Map.Entry<String, Double>> entries = userBalances.entrySet();

        if (entries.isEmpty()) {
            throw new IllegalStateException("No active balances found in 'userBalances' map.");
        }

        return new ArrayList<>(entries);
    }
}




