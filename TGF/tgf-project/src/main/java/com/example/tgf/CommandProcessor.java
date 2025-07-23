package com.example.tgf;

import java.util.Scanner;

public class CommandProcessor {
    private final MsisdnManager msisdnManager;
    private final ChfClient chfClient;
    private Thread simThread;

    public CommandProcessor(MsisdnManager msisdnManager, ChfClient chfClient) {
        this.msisdnManager = msisdnManager;
        this.chfClient = chfClient;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Komutlar: updateMsisdn | start | stop | terminate");

        while (true) {
            System.out.print("> ");
            String cmd = scanner.nextLine().trim();
            processCommand(cmd); // kullanıcıdan gelen komutu işle
        }
    }

    public void processCommand(String cmd) {
        switch (cmd) {
            case "updateMsisdn":
                msisdnManager.updateList();
                break;

            case "start":
                if (simThread != null && simThread.isAlive()) {
                    System.out.println("Simülasyon zaten çalışıyor.");
                } else {
                    simThread = new Thread(new TrafficSimulator(msisdnManager, chfClient));
                    simThread.start();
                    System.out.println("Simülasyon başlatıldı.");
                }
                break;

            case "stop":
                if (simThread != null && simThread.isAlive()) {
                    simThread.interrupt();
                    simThread = null;
                    System.out.println("Simülasyon durduruldu.");
                } else {
                    System.out.println("Zaten durdurulmuş.");
                }
                break;

            case "terminate":
                System.out.println("Uygulama kapatılıyor...");
                msisdnManager.stopAutoUpdate();
                if (simThread != null) simThread.interrupt();
                System.exit(0);
                break;

            default:
                System.out.println("Geçersiz komut.");
        }
    }
}
