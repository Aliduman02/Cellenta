package com.example.tgf;

public class Main {
    public static void main(String[] args) {
        MsisdnManager msisdnManager = new MsisdnManager();
        ChfClient chfClient = new ChfClient();
        CommandProcessor processor = new CommandProcessor(msisdnManager, chfClient);
        processor.start();
    }
}
