package com.i2i.intern.cellenta.chf.kafka;

import com.i2i.intern.cellenta.chf.config.KafkaConfig;
import com.i2i.intern.cellenta.chf.model.AbmfMessage;
import com.i2i.intern.cellenta.chf.model.CgfMessage;
import com.i2i.intern.cellenta.chf.model.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class KafkaProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaConfig kafkaConfig;
    private final Executor kafkaTaskExecutor;
    
    @Autowired
    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate, 
                        KafkaConfig kafkaConfig,
                        @Qualifier("kafkaTaskExecutor") Executor kafkaTaskExecutor) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaConfig = kafkaConfig;
        this.kafkaTaskExecutor = kafkaTaskExecutor;
    }
    
    /**
     * ABMF'e balance güncelleme mesajı gönder - ASYNC
     * @param abmfMessage ABMF mesajı
     */
    @Async("kafkaTaskExecutor")
    public CompletableFuture<Void> sendToAbmfAsync(AbmfMessage abmfMessage) {
        String topic = kafkaConfig.getTopics().getAbmf();
        String key = abmfMessage.getMsisdn();
        
        logger.debug("Sending ASYNC message to ABMF topic: {} for MSISDN: {}", topic, key);
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, abmfMessage);
        
        return future.handle((result, exception) -> {
            if (exception == null) {
                logger.debug("ABMF message sent successfully for MSISDN: {} to topic: {} with offset: {}", 
                           key, topic, result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send ABMF message for MSISDN: {} to topic: {}, Error: {}", 
                           key, topic, exception.getMessage());
            }
            return null;
        });
    }
    
    /**
     * CGF'e usage record mesajı gönder - ASYNC
     * @param cgfMessage CGF mesajı
     */
    @Async("kafkaTaskExecutor")
    public CompletableFuture<Void> sendToCgfAsync(CgfMessage cgfMessage) {
        String topic = kafkaConfig.getTopics().getCgf();
        String key = cgfMessage.getGiverMsisdn();
        
        logger.debug("Sending ASYNC message to CGF topic: {} for MSISDN: {}", topic, key);
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, cgfMessage);
        
        return future.handle((result, exception) -> {
            if (exception == null) {
                logger.debug("CGF message sent successfully for MSISDN: {} to topic: {} with offset: {}", 
                           key, topic, result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send CGF message for MSISDN: {} to topic: {}, Error: {}", 
                           key, topic, exception.getMessage());
            }
            return null;
        });
    }
    
    /**
     * Notification Service'e uyarı mesajı gönder - ASYNC
     * @param notificationMessage Notification mesajı
     */
    @Async("kafkaTaskExecutor")
    public CompletableFuture<Void> sendToNotificationAsync(NotificationMessage notificationMessage) {
        String topic = kafkaConfig.getTopics().getNotification();
        String key = notificationMessage.getMsisdn();
        
        logger.debug("Sending ASYNC message to Notification topic: {} for MSISDN: {} with percentage: {}%", 
                   topic, key, notificationMessage.getPercentage());
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, notificationMessage);
        
        return future.handle((result, exception) -> {
            if (exception == null) {
                logger.debug("Notification message sent successfully for MSISDN: {} to topic: {} with offset: {}", 
                           key, topic, result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send Notification message for MSISDN: {} to topic: {}, Error: {}", 
                           key, topic, exception.getMessage());
            }
            return null;
        });
    }
    
    /**
     * SYNC VERSIONS - Backward compatibility için
     */
    public void sendToAbmf(AbmfMessage abmfMessage) {
        String topic = kafkaConfig.getTopics().getAbmf();
        String key = abmfMessage.getMsisdn();
        
        logger.info("Sending message to ABMF topic: {} for MSISDN: {}", topic, key);
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, abmfMessage);
        
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                logger.info("Successfully sent ABMF message for MSISDN: {} to topic: {} with offset: {}", 
                           key, topic, result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send ABMF message for MSISDN: {} to topic: {}, Error: {}", 
                           key, topic, exception.getMessage());
            }
        });
    }
    
    public void sendToCgf(CgfMessage cgfMessage) {
        String topic = kafkaConfig.getTopics().getCgf();
        String key = cgfMessage.getGiverMsisdn();
        
        logger.info("Sending message to CGF topic: {} for MSISDN: {}", topic, key);
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, cgfMessage);
        
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                logger.info("Successfully sent CGF message for MSISDN: {} to topic: {} with offset: {}", 
                           key, topic, result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send CGF message for MSISDN: {} to topic: {}, Error: {}", 
                           key, topic, exception.getMessage());
            }
        });
    }
    
    public void sendToNotification(NotificationMessage notificationMessage) {
        String topic = kafkaConfig.getTopics().getNotification();
        String key = notificationMessage.getMsisdn();
        
        logger.info("Sending message to Notification topic: {} for MSISDN: {} with percentage: {}%", 
                   topic, key, notificationMessage.getPercentage());
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, notificationMessage);
        
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                logger.info("Successfully sent Notification message for MSISDN: {} to topic: {} with offset: {}", 
                           key, topic, result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send Notification message for MSISDN: {} to topic: {}, Error: {}", 
                           key, topic, exception.getMessage());
            }
        });
    }
    
    /**
     * Tüm topic'lere PARALEL ASYNC mesaj gönder - YENİ OPTIMIZED VERSION
     * @param abmfMessage ABMF mesajı
     * @param cgfMessage CGF mesajı
     * @param notificationMessage Notification mesajı (optional)
     */
    public void sendAllMessages(AbmfMessage abmfMessage, CgfMessage cgfMessage, NotificationMessage notificationMessage) {
        logger.info("Sending PARALLEL ASYNC messages to all topics for MSISDN: {}", abmfMessage.getMsisdn());
        
        // Paralel async mesaj gönderimi
        CompletableFuture<Void> abmfFuture = sendToAbmfAsync(abmfMessage);
        CompletableFuture<Void> cgfFuture = sendToCgfAsync(cgfMessage);
        
        CompletableFuture<Void> allFutures;
        
        if (notificationMessage != null) {
            CompletableFuture<Void> notificationFuture = sendToNotificationAsync(notificationMessage);
            allFutures = CompletableFuture.allOf(abmfFuture, cgfFuture, notificationFuture);
        } else {
            allFutures = CompletableFuture.allOf(abmfFuture, cgfFuture);
        }
        
        // Tüm mesajların sonucunu logla (non-blocking)
        allFutures.whenComplete((result, exception) -> {
            if (exception == null) {
                logger.info("✅ ALL Kafka messages sent SUCCESSFULLY for MSISDN: {} (PARALLEL ASYNC)", abmfMessage.getMsisdn());
            } else {
                logger.error("❌ Some Kafka messages FAILED for MSISDN: {}, Error: {}", 
                           abmfMessage.getMsisdn(), exception.getMessage());
            }
        });
    }
    
    /**
     * Sadece SYNC mesaj gönderimi - Emergency fallback
     */
    public void sendAllMessagesSync(AbmfMessage abmfMessage, CgfMessage cgfMessage, NotificationMessage notificationMessage) {
        logger.info("Sending SYNC messages (fallback) to all topics for MSISDN: {}", abmfMessage.getMsisdn());
        
        // ABMF'e gönder
        sendToAbmf(abmfMessage);
        
        // CGF'e gönder
        sendToCgf(cgfMessage);
        
        // Notification gerekiyorsa gönder
        if (notificationMessage != null) {
            sendToNotification(notificationMessage);
        }
        
        logger.info("All SYNC messages sent for MSISDN: {}", abmfMessage.getMsisdn());
    }
}
