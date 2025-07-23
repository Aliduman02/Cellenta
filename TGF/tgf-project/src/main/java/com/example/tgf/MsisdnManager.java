package com.example.tgf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.i2i.cellenta.hazelcast.service.MsisdnService;

public class MsisdnManager {
    private List<String> msisdnList;
    private final Random random = new Random();

    public MsisdnManager() {
        // Hazelcast'e bağlanmayı MsisdnService yönetecek
        msisdnList = new ArrayList<>(MsisdnService.getAllRegisteredMsisdns());
        System.out.println("Başlangıçta " + msisdnList.size() + " MSISDN yüklendi.");
    }

    public void updateList() {
        msisdnList = new ArrayList<>(MsisdnService.getAllRegisteredMsisdns());
        System.out.println("Güncellendi: " + msisdnList.size() + " MSISDN bulundu.");
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

    private volatile boolean updating = true;

    public void startAutoUpdate() {
        Thread updater = new Thread(() -> {
            while (updating) {
                try {
                    Thread.sleep(30000);
                    System.out.println("[AUTO-UPDATE] MSISDN listesi güncelleniyor...");
                    updateList();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                     System.out.println("[AUTO-UPDATE] Güncelleme iptal edildi.");
                    break;
                }
            }
        });
        updater.setDaemon(true); // Program kapanınca otomatik sonlanır
        updater.start();
    }

    public void stopAutoUpdate() {
        updating = false;
    }
}
