package com.i2i.intern.cellenta.chf.kafka;

import com.i2i.intern.cellenta.chf.config.KafkaConfig;
import com.i2i.intern.cellenta.chf.model.AbmfMessage;
import com.i2i.intern.cellenta.chf.model.CgfMessage;
import com.i2i.intern.cellenta.chf.model.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaConfig kafkaConfig;
    
    @Autowired
    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate, KafkaConfig kafkaConfig) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaConfig = kafkaConfig;
    }
    
    /**
     * ABMF'e balance güncelleme mesajı gönder
     * @param abmfMessage ABMF mesajı
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
    
    /**
     * CGF'e usage record mesajı gönder
     * @param cgfMessage CGF mesajı
     */
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
    
    /**
     * Notification Service'e uyarı mesajı gönder
     * @param notificationMessage Notification mesajı
     */
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
     * Tüm topic'lere mesaj gönder (bulk operation)
     * @param abmfMessage ABMF mesajı
     * @param cgfMessage CGF mesajı
     * @param notificationMessage Notification mesajı (optional)
     */
    public void sendAllMessages(AbmfMessage abmfMessage, CgfMessage cgfMessage, NotificationMessage notificationMessage) {
        logger.info("Sending messages to all topics for MSISDN: {}", abmfMessage.getMsisdn());
        
        // ABMF'e gönder
        sendToAbmf(abmfMessage);
        
        // CGF'e gönder
        sendToCgf(cgfMessage);
        
        // Notification gerekiyorsa gönder (%80 veya %100 kullanım)
        if (notificationMessage != null) {
            sendToNotification(notificationMessage);
        }
        
        logger.info("All messages sent for MSISDN: {}", abmfMessage.getMsisdn());
    }
}
