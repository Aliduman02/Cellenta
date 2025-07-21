package com.example.tgf;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class MsisdnMapCleaner {

    public static void main(String[] args) {
        // Hazelcast client yapılandırması
        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress("104.198.77.152:5701");

        // Hazelcast'e bağlan
        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);

        try {
            // msisdnMap haritasını al
            IMap<String, String> msisdnMap = client.getMap("registeredMsisdn");

            // Haritayı temizle
            msisdnMap.clear();

            System.out.println("msisdnMap içeriği başarıyla silindi.");

        } catch (Exception e) {
            System.err.println("msisdnMap silinirken hata oluştu: " + e.getMessage());
        } finally {
            // Bağlantıyı kapat
            client.shutdown();
        }
    }
}
