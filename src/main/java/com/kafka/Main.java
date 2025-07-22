package com.kafka;

import com.kafka.message.*;
import com.kafka.KafkaTopicConstants;
import com.kafka.health.HealthChecker;
import com.kafka.metrics.MetricsCollector;
import com.kafka.retry.RetryHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import com.kafka.message.UsageRecordMessage;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final HealthChecker healthChecker = new HealthChecker();
    private static final MetricsCollector metricsCollector = new MetricsCollector();
    // Retry handler removed to avoid infinite retry loops

    public static void main(String[] args) {
        // Parse command line arguments
        String specificTopic = null;
        logger.info("Command line arguments: {}", String.join(" ", args));
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--topic=")) {
                specificTopic = args[i].substring("--topic=".length());
                logger.info("Command line argument detected: --topic={}", specificTopic);
                break;
            } else if (args[i].equals("--topic") && i + 1 < args.length) {
                specificTopic = args[i + 1];
                logger.info("Command line argument detected: --topic {}", specificTopic);
                break;
            }
        }
        
        logger.info("=== Kafka Router Başlatıldı ===");
        logger.info("Kafka Sunucu: 34.38.128.100:9092,9093,9094");
        logger.info("Kafdrop Dashboard: http://34.38.128.100:9000/");
        if (specificTopic != null) {
            logger.info("Specific Topic: {} - Sadece bu topic dinlenecek", specificTopic);
        } else {
            logger.info("Tüm topic'ler dinlenecek");
        }
        logger.info("================================");

        // Test mode kontrolü
        boolean testMode = System.getProperty("test.messages", "false").equals("true") || 
                          System.getenv("test_messages") != null;
        
        if (testMode) {
            logger.info("=== Test Mode Aktif - Test Message'ları Üretiliyor ===");
            
            // Create and send random NotificationMessage using instance-based producer
            try (MessageProducer<NotificationMessage> notificationProducer = new MessageProducer<>()) {
                notificationProducer.createNotificationMessageProducer();
                for (int i = 0; i < 40; i++) {
                    NotificationMessage notificationMessage = generateRandomNotificationMessage();
                    notificationProducer.send(notificationMessage, KafkaTopicConstants.USAGE_RECORD_TOPIC);
                    metricsCollector.incrementMessagesSent();
                    metricsCollector.incrementTopicMessageCount(KafkaTopicConstants.USAGE_RECORD_TOPIC);
                }
            }

            // Create and send random UsageRecordMessage using instance-based producer
            try (MessageProducer<UsageRecordMessage> usageRecordProducer = new MessageProducer<>()) {
                usageRecordProducer.createUsageRecordMessageProducer();
                for (int i = 0; i < 40; i++) {
                    UsageRecordMessage usageRecordMessage = generateRandomUsageRecordMessage();
                    usageRecordProducer.send(usageRecordMessage, KafkaTopicConstants.CGF_USAGE_TOPIC);
                    metricsCollector.incrementMessagesSent();
                    metricsCollector.incrementTopicMessageCount(KafkaTopicConstants.CGF_USAGE_TOPIC);
                }
            }

            // Create and send random BalanceMessage using instance-based producer
            try (MessageProducer<BalanceMessage> balanceProducer = new MessageProducer<>()) {
                balanceProducer.createBalanceMessageProducer();
                for (int i = 0; i < 40; i++) {
                    BalanceMessage balanceMessage = generateRandomBalanceMessage();
                    balanceProducer.send(balanceMessage, KafkaTopicConstants.ABMF_USAGE_TOPIC);
                    metricsCollector.incrementMessagesSent();
                    metricsCollector.incrementTopicMessageCount(KafkaTopicConstants.ABMF_USAGE_TOPIC);
                }
            }
            
            logger.info("=== Test Message'ları Tamamlandı ===");
        } else {
            logger.info("=== Production Mode - Sadece Consumer'lar Aktif ===");
        }

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("=== Uygulama Kapatılıyor ===");
            logger.info("Consumer'lar kapatılıyor...");
            logger.info("Kafka bağlantıları temizleniyor...");
            
            // Final health and metrics report
            healthChecker.logHealthStatus();
            metricsCollector.logMetrics();
            
            logger.info("Uygulama güvenli şekilde kapatıldı.");
        }));

        // Initialize consumers based on specific topic or all topics
        final MessageConsumer<BalanceMessage> balanceConsumer;
        final MessageConsumer<UsageRecordMessage> usageRecordConsumer;
        final MessageConsumer<NotificationMessage> notificationConsumer;
        
        if (specificTopic == null || specificTopic.equals("chf-to-abmf")) {
            balanceConsumer = new MessageConsumer<>();
            balanceConsumer.createBalanceMessageConsumer();
        } else {
            balanceConsumer = null;
        }
        
        if (specificTopic == null || specificTopic.equals("chf-to-cgf")) {
            usageRecordConsumer = new MessageConsumer<>();
            usageRecordConsumer.createUsageRecordMessageConsumer();
        } else {
            usageRecordConsumer = null;
        }
        
        if (specificTopic == null || specificTopic.equals("chf-to-notification")) {
            notificationConsumer = new MessageConsumer<>();
            notificationConsumer.createNotificationMessageConsumer();
        } else {
            notificationConsumer = null;
        }

        // Thread-safe shutdown flag
        final boolean[] shutdownRequested = {false};
        
        // Add shutdown hook to close consumers safely
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                shutdownRequested[0] = true;
                logger.info("Shutdown requested, waiting for consumers to finish...");
                
                // Wait a bit for current poll operations to complete
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Close consumers safely
                try {
                    if (balanceConsumer != null && balanceConsumer.consumer != null) {
                        balanceConsumer.consumer.wakeup();
                        balanceConsumer.consumer.close();
                    }
                    if (usageRecordConsumer != null && usageRecordConsumer.consumer != null) {
                        usageRecordConsumer.consumer.wakeup();
                        usageRecordConsumer.consumer.close();
                    }
                    if (notificationConsumer != null && notificationConsumer.consumer != null) {
                        notificationConsumer.consumer.wakeup();
                        notificationConsumer.consumer.close();
                    }
                    logger.info("Consumers closed safely.");
                } catch (Exception e) {
                    logger.error("Error closing consumers: {}", e.getMessage());
                }
            }));

        // Poll messages in an infinite loop with graceful shutdown
        while (!shutdownRequested[0]) {
            try {
                if (balanceConsumer != null) {
                    pollAndPrint(balanceConsumer);
                }
                if (usageRecordConsumer != null) {
                    pollAndPrint(usageRecordConsumer);
                }
                if (notificationConsumer != null) {
                    pollAndPrint(notificationConsumer);
                }

                Thread.sleep(1000); // Adjust polling interval as needed
                
                // Log health and metrics every 30 seconds
                if (System.currentTimeMillis() % 30000 < 1000) {
                    healthChecker.logHealthStatus();
                    metricsCollector.logMetrics();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
                            } catch (Exception e) {
                    if (shutdownRequested[0]) {
                        break;
                    }
                    logger.error("Error during polling: {}", e.getMessage());
                    healthChecker.incrementMessagesFailed();
                    metricsCollector.incrementMessagesFailed();
                    metricsCollector.incrementErrorCount("polling_error");
                    
                    // Simple error handling - continue polling without retry
                    logger.info("Continuing with next poll cycle...");
                }
            }
            
            logger.info("Main loop terminated, application shutting down...");
    }

    private static <T extends Message> void pollAndPrint(MessageConsumer<T> consumer) {
        ConsumerRecords<String, T> records = consumer.poll();
        if (records != null) {
            for (ConsumerRecord<String, T> record : records) {
                if (record.value() instanceof com.kafka.message.UsageRecordMessage usageRecordMessage) {
                    if (usageRecordMessage.getReceiverMsisdn() == null) {
                        logger.warn("CGF mesajında receiver_msisdn NULL! Mesaj: {}", usageRecordMessage);
                    }
                    // Her durumda mesajı logla
                    logger.info("Topic: {} | Key: {} | Value: {}", 
                        record.topic(), record.key(), record.value().toString());
                } else {
                    logger.info("Topic: {} | Key: {} | Value: {}", 
                        record.topic(), record.key(), record.value().toString());
                }
                // Update metrics
                healthChecker.incrementMessagesProcessed();
                metricsCollector.incrementMessagesProcessed();
                metricsCollector.incrementMessagesReceived();
                metricsCollector.incrementTopicMessageCount(record.topic());
            }
        }
    }

    // Helper methods to generate random messages

    private static NotificationMessage generateRandomNotificationMessage() {
        // Örnek: NotificationMessage JSON formatına uygun üretim
        return new NotificationMessage(
            "5551234567", // msisdn
            "minutes", // usage_type
            80, // percentage
            20, // remaining_amount
            "WARNING", // notification_message
            "Süper İnternet", // package_name
            "2025-07-18 13:37:35", // package_start_date
            "2025-08-17 13:37:35", // package_end_date
            100, // total_minutes
            100, // total_sms
            20000, // total_data
            20, // remaining_minutes
            45, // remaining_sms
            8500, // remaining_data
            "2025-07-20 12:31:57" // timestamp
        );
    }

    private static UsageRecordMessage generateRandomUsageRecordMessage() {
        // Örnek veri, yeni constructor'a uygun şekilde
        return new UsageRecordMessage(
            "5551234567", // giver_msisdn
            "5559876543", // receiver_msisdn
            "2025-07-20 12:31:57", // usage_date
            "minutes", // usage_type
            10 // usage_duration
        );
    }
    private static BalanceMessage generateRandomBalanceMessage() {
        // Örnek veri, yeni constructor'a uygun şekilde
        return new BalanceMessage(
            "5551234567", // msisdn
            7, // new_minutes
            39, // new_sms
            16732, // new_data
            "2025-07-20 12:31:57" // timestamp
        );
    }
} 