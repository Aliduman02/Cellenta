package com.i2i.cellenta.hazelcast.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.i2i.cellenta.hazelcast.model.UserQuota;
import com.i2i.cellenta.hazelcast.config.HazelcastConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserQuotaService {
    private static final HazelcastInstance hz = HazelcastConnector.getInstance();
    private static final String MAP_NAME = "userQuotas";

    public static void addUserQuota(String msisdn, int minutes, int sms, int data) {
        IMap<String, UserQuota> userQuotas = hz.getMap(MAP_NAME);

        if (msisdn == null) {
            throw new IllegalArgumentException("MSISDN cannot be null.");
        }

        if (userQuotas.containsKey(msisdn)) {
            throw new IllegalArgumentException("User '" + msisdn + "' already exists.");
        }

        userQuotas.put(msisdn, new UserQuota(minutes, sms, data));
    }

    public static UserQuota getUserQuota(String msisdn) {
        IMap<String, UserQuota> userQuotas = hz.getMap(MAP_NAME);

        if (msisdn == null) {
            throw new IllegalArgumentException("MSISDN cannot be null.");
        }
        if (!userQuotas.containsKey(msisdn)) {
            throw new IllegalArgumentException("MSISDN '" + msisdn + "' not found.");
        }

        return userQuotas.get(msisdn);
        // or to return a String of return userQuotas.get(msisdn).toString;

    }

    public static int getUserMinutes(String msisdn) {
        IMap<String, UserQuota> userQuotas = hz.getMap(MAP_NAME);

        if (msisdn == null) {
            throw new IllegalArgumentException("MSISDN cannot be null.");
        }
        if (!userQuotas.containsKey(msisdn)) {
            throw new IllegalArgumentException("MSISDN '" + msisdn + "' not found.");
        }

        return userQuotas.get(msisdn).getMinutes();
    }

    public static int getUserSms(String msisdn) {
        IMap<String, UserQuota> userQuotas = hz.getMap(MAP_NAME);

        if (msisdn == null) {
            throw new IllegalArgumentException("MSISDN cannot be null.");
        }
        if (!userQuotas.containsKey(msisdn)) {
            throw new IllegalArgumentException("MSISDN '" + msisdn + "' not found.");
        }

        return userQuotas.get(msisdn).getSms();
    }

    public static int getUserData(String msisdn) {
        IMap<String, UserQuota> userQuotas = hz.getMap(MAP_NAME);

        if (msisdn == null) {
            throw new IllegalArgumentException("MSISDN cannot be null.");
        }
        if (!userQuotas.containsKey(msisdn)) {
            throw new IllegalArgumentException("MSISDN '" + msisdn + "' not found.");
        }

        return userQuotas.get(msisdn).getData();

    }

    public static boolean updateUserQuotaBy(String msisdn, int minutes, int sms, int data) {
        IMap<String, UserQuota> userQuotas = hz.getMap(MAP_NAME);

        if (msisdn == null) {
            throw new IllegalArgumentException("MSISDN cannot be null.");
        }

        if (!userQuotas.containsKey(msisdn)) {
            throw new IllegalArgumentException("MSISDN '" + msisdn + "' not found.");
        }

        userQuotas.computeIfPresent(msisdn, (key, quota) -> {
            quota.setMinutes(quota.getMinutes() + minutes);
            quota.setSms(quota.getSms() + sms);
            quota.setData(quota.getData() + data);
            return quota;
        });

        return true;
    }

    public static int size() {
        IMap<String, String> registeredUsers = hz.getMap(MAP_NAME);

        if (registeredUsers == null) {
            throw new IllegalArgumentException("Hazelcast map 'userQuotas' is null.");
        }

        return registeredUsers.size();
    }


    public static boolean removeUserQuota(String msisdn) {
        IMap<String, UserQuota> userQuotas = hz.getMap(MAP_NAME);

        if (!userQuotas.containsKey(msisdn)) {
            throw new IllegalArgumentException("MSISDN '" + msisdn + "' does not exist.");
        }

        UserQuota removed = userQuotas.remove(msisdn);

        if (removed == null) {
            throw new IllegalArgumentException("Removal failed for MSISDN '" + msisdn + "'.");
        }
        System.out.println(msisdn + " was removed successfully ");
        return true;
    }

    public static boolean removeAllUserQuota() {
        IMap<String, UserQuota> userQuotas = hz.getMap(MAP_NAME);

        if (userQuotas.isEmpty()) {
            throw new IllegalArgumentException("The map is empty. Nothing to remove.");
        }

        userQuotas.clear();
        System.out.println("All user quotas removed successfully.");
        return true;
    }



    public static List<Map.Entry<String, UserQuota>> getAllUserQuotas() {
        IMap<String, UserQuota> userQuotas = hz.getMap(MAP_NAME);

        if (userQuotas == null) {
            throw new IllegalArgumentException("Hazelcast map 'userQuotas' is null.");
        }

        Set<Map.Entry<String, UserQuota>> entries = userQuotas.entrySet();

        if (entries.isEmpty()) {
            throw new IllegalArgumentException("No active balances found in 'userQuotas' map.");
        }

        return new ArrayList<>(entries);
    }

}

