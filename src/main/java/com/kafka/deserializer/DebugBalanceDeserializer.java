package com.kafka.deserializer;

import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class DebugBalanceDeserializer implements Deserializer<String> {
    private static final Logger logger = LoggerFactory.getLogger(DebugBalanceDeserializer.class);

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public String deserialize(String topic, byte[] data) {
        if (data == null) {
            logger.info("Topic: {} | Received null data", topic);
            return null;
        }
        
        try {
            // Raw byte array'i string'e çevir
            String rawMessage = new String(data, "UTF-8");
            logger.info("Topic: {} | Raw message: {}", topic, rawMessage);
            
            // Hex string de göster
            String hexString = bytesToHex(data);
            logger.info("Topic: {} | Raw data (hex): {}", topic, hexString);
            
            return rawMessage;
        } catch (Exception e) {
            logger.error("Error deserializing message: {}", e.getMessage());
            return null;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    @Override
    public void close() {}
} 