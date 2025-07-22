package com.kafka.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;

public class ConfigValidator {
    private static final Logger logger = LoggerFactory.getLogger(ConfigValidator.class);
    
    public static void validateKafkaConfig(Properties properties) {
        String bootstrapServers = properties.getProperty("kafka.bootstrap.servers");
        
        if (bootstrapServers == null || bootstrapServers.trim().isEmpty()) {
            throw new IllegalArgumentException("kafka.bootstrap.servers is required");
        }
        
        // Validate server format
        String[] servers = bootstrapServers.split(",");
        for (String server : servers) {
            if (!server.contains(":")) {
                throw new IllegalArgumentException("Invalid server format: " + server + ". Expected format: host:port");
            }
        }
        
        logger.info("Kafka configuration validated successfully. Bootstrap servers: {}", bootstrapServers);
    }
    
    public static void validateTopicNames() {
        String[] requiredTopics = {
            "chf-to-notification",
            "chf-to-cgf", 
            "chf-to-abmf"
        };
        
        for (String topic : requiredTopics) {
            if (topic == null || topic.trim().isEmpty()) {
                throw new IllegalArgumentException("Topic name cannot be null or empty: " + topic);
            }
        }
        
        logger.info("Topic names validated successfully");
    }
} 