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
        // connect() çağrısı yok, direkt kullanabilirsin
        msisdnManager.connect(); 
        System.out.println("Komutlar: updateMsisdn | start | stop | terminate");

        while (true) {
            System.out.print("> ");
            String cmd = scanner.nextLine().trim();

            switch (cmd) {
                case "updateMsisdn":
                    msisdnManager.updateList();
                    break;
                case "start":
                    if (simThread != null && simThread.isAlive()) {
                        System.out.println("Simülasyon zaten çalışıyor.");
                        break;
                    }
                    simThread = new Thread(new TrafficSimulator(msisdnManager, chfClient));
                    simThread.start();
                    break;
                case "stop":
                    if (simThread != null) simThread.interrupt();
                    break;
                case "terminate":
                    System.out.println("Uygulama kapatılıyor...");
                    if (simThread != null) simThread.interrupt();
                    return;
                default:
                    System.out.println("Geçersiz komut.");
            }
        }
    }
}
