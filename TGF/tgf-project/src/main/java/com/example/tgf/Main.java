package com.example.tgf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        MsisdnManager msisdnManager = new MsisdnManager();
        msisdnManager.startAutoUpdate(); // Her 60 saniyede bir güncelle
        // Simülasyon başlatılmadan önce MSISDN listesini güncelle
        ChfClient chfClient = new ChfClient();
        CommandProcessor processor = new CommandProcessor(msisdnManager, chfClient);
        processor.processCommand("start");
        if (System.console() != null) {
            processor.start(); // sadece interaktif terminalde çalışıyorsa başlat
        }


    }
}
