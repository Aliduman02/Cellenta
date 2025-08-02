package com.example.tgf;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TrafficSimulator implements Runnable {
    private final MsisdnManager msisdnManager;
    private final ChfClient chfClient;
    private final Random random = new Random();
    private final ExecutorService executor;

    public TrafficSimulator(MsisdnManager msisdnManager, ChfClient chfClient) {
        this.msisdnManager = msisdnManager;
        this.chfClient = chfClient;
        this.executor = Executors.newFixedThreadPool(10); // 10 paralel thread
    }

    @Override
    public void run() {
        String[] usageTypes = { "minutes", "sms", "data" };
        final int totalPerSecond = 1000;

        while (!Thread.currentThread().isInterrupted()) {
            long start = System.currentTimeMillis();

            for (int i = 0; i < totalPerSecond; i++) {
                executor.submit(() -> {
                    String sender = msisdnManager.getRandomMsisdn();
                    String receiver = msisdnManager.getRandomDifferentMsisdn(sender);
                    String usageType = usageTypes[random.nextInt(usageTypes.length)];

                    int amount;
                    switch (usageType) {
                        case "minutes":
                            amount = 1 + random.nextInt(10);
                            break;
                        case "sms":
                            amount = 1 + random.nextInt(3);
                            break;
                        case "data":
                            amount = 10 + random.nextInt(20);
                            break;
                        default:
                            amount = 1;
                    }

                    long timestamp = System.currentTimeMillis();
                    chfClient.sendChargingRequest(sender, usageType, amount, timestamp,
                            usageType.equals("data") ? null : receiver);
                });
            }

            // 1 saniyeyi tamamla
            long elapsed = System.currentTimeMillis() - start;
            long sleepTime = 1000 - elapsed;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        // Kapandığında thread pool kapat
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
