package com.kafka;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final String CONFIG_FILE = "kafka-config.properties"; // Place this file in src/main/resources
    private static final Properties properties = new Properties();

    static {
        try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                System.err.println("Warning: Configuration file not found: " + CONFIG_FILE + ". Using default values.");
                // Set default values
                properties.setProperty("kafka.url", "34.38.128.100:9092,34.38.128.100:9093,34.38.128.100:9094");
                properties.setProperty("kafka.bootstrap.servers", "34.38.128.100:9092,34.38.128.100:9093,34.38.128.100:9094");
            } else {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            System.err.println("Failed to load configuration file: " + CONFIG_FILE + ". Using default values.");
            // Set default values
            properties.setProperty("kafka.url", "34.38.128.100:9092,34.38.128.100:9093,34.38.128.100:9094");
            properties.setProperty("kafka.bootstrap.servers", "34.38.128.100:9092,34.38.128.100:9093,34.38.128.100:9094");
        }
    }

    public static String getProperty(String key) {
        // First, check if the value exists as an environment variable
        String value = System.getenv(key);
        if (value != null) {
            return value;
        }
        // Otherwise, load from the properties file
        return properties.getProperty(key);
    }

    // Backward compatibility methods
    public static String get(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    public static String get(String key) {
        return getProperty(key);
    }
}