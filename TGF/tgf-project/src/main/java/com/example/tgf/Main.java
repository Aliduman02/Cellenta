package com.example.tgf;

public class Main {
    public static void main(String[] args) {
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
