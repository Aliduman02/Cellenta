package com.i2i.cellenta.hazelcast.test;

import com.i2i.cellenta.hazelcast.model.UserQuota;
import com.i2i.cellenta.hazelcast.config.HazelcastConnector;
import com.i2i.cellenta.hazelcast.service.UserBalanceService;
import com.i2i.cellenta.hazelcast.service.UserQuotaService;
import com.i2i.cellenta.hazelcast.service.UserRegistryService;

public class MapsTesting {
    public static void main(String[] args) {



        String userId = "user1";
        double balance = 100.0;
        int minutes = 300;
        int sms = 499;
        int data = 600;

        System.out.println("Registering user...");
        UserRegistryService.registerUser(userId);
        System.out.println("the registered User: " +UserRegistryService.getRegisteredUser( userId));
        UserRegistryService.registerUser("MHJ");
        UserRegistryService.registerUser("ALI");
        UserRegistryService.unregisterUser("ALI");

        System.out.println("returning all the map of: UserRegistryService");
        System.out.println(UserRegistryService.getAllRegisteredUsers());



        // Set balance
        System.out.println("Setting user balance...");
        UserBalanceService.createUserBalance(userId, balance);
        System.out.println("the registered User Balance for "+ userId+ ": " +UserBalanceService.getUserBalance(userId));
        UserBalanceService.createUserBalance("AMR", 343.0);
        UserBalanceService.createUserBalance("APO", 545.3);
        UserBalanceService.removeUserBalance("APO");

        System.out.println("returning all the map of: UserBalanceService");
        System.out.println(UserBalanceService.getAllUserBalances());


        // Set quota
        System.out.println("Setting user quota...");
        UserQuotaService.addUserQuota(userId, minutes,sms,data);
        System.out.println("the registered User Quora for "+ userId+ ": " +UserQuotaService.getUserQuota(userId));
//      or you may use the toString method
//      System.out.println("the registered User Quora for "+ userId+ ": " +UserQuotaService.getUserQuota(userId).toString());

        System.out.println("Updating user quota with usage...");
        UserQuotaService.updateUserQuotaBy(userId, 37,41,9);
        // Check quota again
        UserQuota updatedQuota = UserQuotaService.getUserQuota(userId);
        System.out.println("Remaining Quota: " + (updatedQuota != null ? updatedQuota.toString() : "null"));
        UserQuotaService.addUserQuota("ME", 190,250,1024);
        UserQuotaService.addUserQuota("You", 88,125,2048);

        UserQuotaService.removeUserQuota("ME");

        System.out.println("returning all the map of: UserQuotaService");
        System.out.println(UserQuotaService.getAllUserQuotas());


        System.out.println("All services tested successfully.");
        HazelcastConnector.shutdown();
    }
}
