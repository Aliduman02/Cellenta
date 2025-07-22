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
            String msisdn = msisdnManager.getRandomMsisdn();
            String calledNumber = null;

            if (msisdn != null) {
                String usageType = usageTypes[random.nextInt(usageTypes.length)];
                int amount;

                switch (usageType) {
                    case "minutes":
                        amount = 1 + random.nextInt(60);
                        calledNumber = msisdnManager.getRandomDifferentMsisdn(msisdn);
                        break;
                    case "sms":
                        amount = 1 + random.nextInt(10);
                        calledNumber = msisdnManager.getRandomDifferentMsisdn(msisdn);
                        break;
                    case "data":
                        amount = 1+ random.nextInt(500);
                        break;
                    default:
                        amount = 1;
                }

                long timestamp = System.currentTimeMillis();
                chfClient.sendChargingRequest(msisdn, usageType, amount, timestamp, calledNumber);
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println("Simülasyon durduruldu.");
                break;
            }
        }
    }
}
