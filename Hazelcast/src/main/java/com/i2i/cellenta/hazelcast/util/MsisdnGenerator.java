package com.i2i.cellenta.hazelcast.util;

import java.util.Random;

public class MsisdnGenerator {
    private static final Random RANDOM = new Random();
    public static String generateRandomMsisdn() {
        long num = Math.abs(RANDOM.nextLong() % 1_000_000_000L);
        return "05" + String.format("%09d", num);
    }
}