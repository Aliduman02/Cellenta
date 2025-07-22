package com.example.tgf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class MsisdnManager {
    private HazelcastInstance client;
    private List<String> msisdnList = new ArrayList<>();
    private final Random random = new Random();

    // Hazelcast'e bağlan
    public void connect() {
        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress("104.198.77.152:5701");
        client = HazelcastClient.newHazelcastClient(config);
    }

    // msisdnList içine değerleri (telefon numaralarını) al
    public void updateList() {
        if (client == null) {
            System.err.println("Hazelcast client bağlı değil. Önce connect() çağrılmalı.");
            return;
        }

        try {
            IMap<String, String> registeredUsers = client.getMap("registeredMsisdn");
            msisdnList = new ArrayList<>(registeredUsers.values()); // 🔥 DEĞERLERİ ALIYORUZ
            System.out.println("Hazelcast'ten " + msisdnList.size() + " MSISDN alındı.");
        } catch (Exception e) {
            System.err.println("MSISDN verileri alınırken hata oluştu: " + e.getMessage());
        }
        System.out.println("MSISDN Listesi:");
        for (String msisdn : msisdnList) {
            System.out.println(msisdn);
        }
        
    }
    public List<String> getMsisdnList() {
        return msisdnList;
    }

    public String getRandomMsisdn() {
        if (msisdnList.isEmpty()) return null;
        return msisdnList.get(random.nextInt(msisdnList.size()));
    }

    public String getRandomDifferentMsisdn(String exclude) {
        if (msisdnList.size() < 2) return null;

        String selected;
        do {
            selected = getRandomMsisdn();
        } while (selected.equals(exclude));

        return selected;
    }
}
