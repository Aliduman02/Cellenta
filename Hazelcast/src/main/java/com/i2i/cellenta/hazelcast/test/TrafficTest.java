package com.i2i.cellenta.hazelcast.test;

import com.i2i.cellenta.hazelcast.model.UserQuota;
import com.i2i.cellenta.hazelcast.service.UserQuotaService;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrafficTest {
    private static final Random random = new Random();
    private static final int MAX_USERS = 10; // Maximum number of users to simulate
    private static final int INTERVAL_SECONDS = 5; // Interval for generating traffic

    public static void main(String[] args) {
        TrafficTest simulator = new TrafficTest();
        simulator.startSimulation();
    }

    public void startSimulation() {
        // Initialize a scheduler to run the simulation periodically
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(this::simulateTraffic, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);

        // Add a shutdown hook to gracefully stop the simulator
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down simulator...");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }));
    }

    private void simulateTraffic() {
        try {
            System.out.println("Simulating user activity...");

            // Step 1: Add new users if there are fewer than MAX_USERS
            addNewUsers();

            // Step 2: Simulate usage for existing users
            simulateUsageForExistingUsers();

            // Step 3: Print current state of all user quotas
            printAllUserQuotas();

        } catch (Exception e) {
            System.err.println("Error during simulation: " + e.getMessage());
        }
    }

    private void addNewUsers() {
        List<Map.Entry<String, UserQuota>> existingUsers = null;
        try {
            existingUsers = UserQuotaService.getAllUserQuotas();
        } catch (IllegalStateException e) {
            // No users exist yet, proceed to add new ones
        }

        int currentUserCount = existingUsers != null ? existingUsers.size() : 0;
        int usersToAdd = random.nextInt(MAX_USERS - currentUserCount + 1);

        for (int i = 0; i < usersToAdd; i++) {
            String msisdn = generateRandomMsisdn();
            try {
                // Add a new user with initial quotas
                int initialMinutes = random.nextInt(1000); // Up to 1000 minutes
                int initialSms = random.nextInt(500);     // Up to 500 SMS
                int initialData = random.nextInt(10000);  // Up to 10000 MB of data
                UserQuotaService.addUserQuota(msisdn, initialMinutes, initialSms, initialData);
                System.out.println("Added new user: " + msisdn + " with quotas - Minutes: " + initialMinutes + ", SMS: " + initialSms + ", Data: " + initialData);
            } catch (IllegalStateException e) {
                // User already exists, skip
                System.out.println("User " + msisdn + " already exists, skipping...");
            }
        }
    }

    private void simulateUsageForExistingUsers() {
        List<Map.Entry<String, UserQuota>> existingUsers = null;
        try {
            existingUsers = UserQuotaService.getAllUserQuotas();
        } catch (IllegalStateException e) {
            System.out.println("No users exist yet to simulate usage.");
            return;
        }

        for (Map.Entry<String, UserQuota> entry : existingUsers) {
            String msisdn = entry.getKey();
            try {
                // Simulate usage by updating quotas (can be positive or negative)
                int minutesUsed = random.nextInt(100) - 20; // Random usage between -20 and +80 minutes
                int smsUsed = random.nextInt(50) - 10;      // Random usage between -10 and +40 SMS
                int dataUsed = random.nextInt(1000) - 200;  // Random usage between -200 and +800 MB

                boolean updated = UserQuotaService.updateUserQuotaBy(msisdn, minutesUsed, smsUsed, dataUsed);
                if (updated) {
                    System.out.println("Updated quotas for user: " + msisdn + " - Minutes: " + minutesUsed + ", SMS: " + smsUsed + ", Data: " + dataUsed);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Error updating quotas for user " + msisdn + ": " + e.getMessage());
            }
        }
    }

    private void printAllUserQuotas() {
        try {
            List<Map.Entry<String, UserQuota>> allQuotas = UserQuotaService.getAllUserQuotas();
            System.out.println("Current state of all user quotas:");
            for (Map.Entry<String, UserQuota> entry : allQuotas) {
                System.out.println("User: " + entry.getKey() + " -> " + entry.getValue());
            }
        } catch (IllegalStateException e) {
            System.out.println("No user quotas available to display.");
        }
    }

    private String generateRandomMsisdn() {
        // Generate a random MSISDN (e.g., a phone number starting with + followed by 10 digits)
        StringBuilder msisdn = new StringBuilder("+");
        for (int i = 0; i < 10; i++) {
            msisdn.append(random.nextInt(10));
        }
        return msisdn.toString();
    }
}