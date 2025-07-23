package com.example.tgf;

import java.util.Random;

public class TrafficSimulator implements Runnable {
    private final MsisdnManager msisdnManager;
    private final ChfClient chfClient;
    private final Random random = new Random();

    public TrafficSimulator(MsisdnManager msisdnManager, ChfClient chfClient) {
        this.msisdnManager = msisdnManager;
        this.chfClient = chfClient;
    }

    @Override
    public void run() {
        String[] usageTypes = {"minutes", "sms", "data"};

        while (!Thread.currentThread().isInterrupted()) {
            for (int i = 0; i < 300; i++) { // saniyede ~300 istek simülasyonu için ayarlandı
                String sender = msisdnManager.getRandomMsisdn();
                String receiver = msisdnManager.getRandomDifferentMsisdn(sender);
                String usageType = usageTypes[random.nextInt(usageTypes.length)];

                int amount;
                switch (usageType) {
                    case "minutes":
                        amount = 1 + random.nextInt(60);
                        break;
                    case "sms":
                        amount = 1 + random.nextInt(10);
                        break;
                    case "data":
                        amount = 10 + random.nextInt(500);
                        break;
                    default:
                        amount = 1;
                }

                long timestamp = System.currentTimeMillis();
                chfClient.sendChargingRequest(sender, usageType, amount, timestamp, usageType.equals("data") ? null : receiver);
            }

            try {
                Thread.sleep(1000); 
            } catch (InterruptedException e) {
                System.out.println("Simülasyon durduruldu.");
                break;
            }
        }
    }
}
