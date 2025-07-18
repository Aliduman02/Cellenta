package com.i2i.cellenta.hazelcast.test;

import com.i2i.cellenta.hazelcast.model.UserQuota;
import com.i2i.cellenta.hazelcast.config.HazelcastConnector;
import com.i2i.cellenta.hazelcast.service.MsisdnService;
import com.i2i.cellenta.hazelcast.service.UserWalletService;
import com.i2i.cellenta.hazelcast.service.UserQuotaService;
import com.i2i.cellenta.hazelcast.util.MsisdnGenerator;

public class MapsTesting {
    public static void main(String[] args) {



        String userId = "user1";
        int balance = 100;
        int minutes = 300;
        int sms = 499;
        int data = 600;

        System.out.println("Registering user...");
//        MsisdnService.unregisterAllMsisdns();
        UserQuotaService.removeAllUserQuota();
        UserWalletService.removeAllWallets();

//        MsisdnService.registerMsisdn(userId);
//        MsisdnService.registerMsisdn("MHJ");
        MsisdnService.registerMsisdn("ALI");
        MsisdnService.unregisterMsisdn("ALI");

        System.out.println("returning all the map of: MsisdnService");
        System.out.println(MsisdnService.getAllRegisteredMsisdns());
        String[] run = new String[10];
        for (int i = 0; i < 10 ; i++) {
            run[i] = MsisdnGenerator.generateRandomMsisdn();
            MsisdnService.registerMsisdn(run[i]);
            System.out.println(run[i]);
        }
        System.out.println(MsisdnService.getAllRegisteredMsisdns());
        System.out.println(MsisdnService.size());



        // Set balance
        System.out.println("Setting user balance...");
        UserWalletService.createWallet(userId, balance);
        System.out.println("the registered User Balance for "+ userId+ ": " +UserWalletService.getWalletAmount(userId));
        UserWalletService.createWallet("AMR", 343);
        UserWalletService.createWallet("APO", 545);
        UserWalletService.removeWallet("APO");

        System.out.println("returning all the map of: UserWalletService");
        System.out.println(UserWalletService.getAllWallets());


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
