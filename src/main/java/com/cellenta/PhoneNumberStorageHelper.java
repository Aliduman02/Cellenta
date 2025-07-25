package com.cellenta;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PhoneNumberStorageHelper {
    private static final String STORAGE_DIR = System.getProperty("user.home") + File.separator + ".cellenta";
    private static final String STORAGE_FILE = STORAGE_DIR + File.separator + "remembered_phones.txt";
    private static final int MAX_STORED_PHONES = 5;

    public static void savePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return;
        }

        try {
            createStorageDirectory();
            
            List<String> phones = getStoredPhoneNumbers();
            
            phones.remove(phoneNumber);
            phones.add(0, phoneNumber);
            
            if (phones.size() > MAX_STORED_PHONES) {
                phones = phones.subList(0, MAX_STORED_PHONES);
            }
            
            writePhonesToFile(phones);
        } catch (Exception e) {
            System.err.println("Error saving phone number: " + e.getMessage());
        }
    }

    public static List<String> getStoredPhoneNumbers() {
        List<String> phones = new ArrayList<>();
        
        try {
            Path filePath = Paths.get(STORAGE_FILE);
            if (Files.exists(filePath)) {
                List<String> lines = Files.readAllLines(filePath);
                for (String line : lines) {
                    String phone = line.trim();
                    if (!phone.isEmpty() && !phones.contains(phone)) {
                        phones.add(phone);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading stored phone numbers: " + e.getMessage());
        }
        
        return phones;
    }

    public static List<String> getSuggestionsForInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ArrayList<>(); // Return empty list when no input - don't show all numbers automatically
        }
        
        List<String> allPhones = getStoredPhoneNumbers();
        List<String> suggestions = new ArrayList<>();
        
        String inputTrimmed = input.trim();
        
        for (String phone : allPhones) {
            if (phone.startsWith(inputTrimmed)) {
                suggestions.add(phone);
            }
        }
        
        return suggestions;
    }

    public static void removePhoneNumber(String phoneNumber) {
        try {
            List<String> phones = getStoredPhoneNumbers();
            phones.remove(phoneNumber);
            writePhonesToFile(phones);
        } catch (Exception e) {
            System.err.println("Error removing phone number: " + e.getMessage());
        }
    }

    public static void clearAllPhones() {
        try {
            Path filePath = Paths.get(STORAGE_FILE);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (Exception e) {
            System.err.println("Error clearing phone numbers: " + e.getMessage());
        }
    }

    private static void createStorageDirectory() throws IOException {
        Path dirPath = Paths.get(STORAGE_DIR);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }

    private static void writePhonesToFile(List<String> phones) throws IOException {
        Path filePath = Paths.get(STORAGE_FILE);
        Files.write(filePath, phones);
    }
}