package com.i2i.intern.cellenta.chf.service;

import com.i2i.intern.cellenta.chf.client.VoltDbClient;
import com.i2i.intern.cellenta.chf.kafka.KafkaProducer;
import com.i2i.intern.cellenta.chf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ChfService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChfService.class);
    
    private final VoltDbClient voltDbClient;
    private final KafkaProducer kafkaProducer;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    public ChfService(VoltDbClient voltDbClient, KafkaProducer kafkaProducer) {
        this.voltDbClient = voltDbClient;
        this.kafkaProducer = kafkaProducer;
    }
    
    /**
     * Ana CHF işlemi - TGF'den gelen kullanım isteğini işle
     * @param usageRequest TGF'den gelen kullanım isteği
     * @return CHF Response (SUCCESS/FAILED)
     */
    public Mono<ChfResponse> processUsageRequest(UsageRequest usageRequest) {
        logger.info("Processing usage request for MSISDN: {}, Type: {}, Amount: {}", 
                   usageRequest.getMsisdn(), usageRequest.getUsageType(), usageRequest.getAmount());
        
        return voltDbClient.getBalance(usageRequest.getMsisdn())
                .flatMap(balanceResponse -> {
                    // Balance response'ını kontrol et
                    if (!"SUCCESS".equals(balanceResponse.getStatus()) || 
                        balanceResponse.getResults() == null || 
                        balanceResponse.getResults().isEmpty()) {
                        
                        return Mono.just(createFailedResponse(usageRequest.getMsisdn(), 
                                                            "Balance information not found"));
                    }
                    
                    VoltDbBalanceResponse.BalanceResult balance = balanceResponse.getResults().get(0);
                    
                    // Paket süresi kontrolü
                    if (isPackageExpired(balance.getEndDate())) {
                        return Mono.just(createFailedResponse(usageRequest.getMsisdn(), 
                                                            "Package expired"));
                    }
                    
                    // Bakiye yeterliliği kontrolü
                    if (!isSufficientBalance(balance, usageRequest)) {
                        return Mono.just(createFailedResponse(usageRequest.getMsisdn(), 
                                                            "Insufficient balance"));
                    }
                    
                    // Yeni balance'ları hesapla
                    VoltDbUpdateRequest updateRequest = calculateNewBalance(balance, usageRequest);
                    
                    // VoltDB'yi güncelle
                    return voltDbClient.updateBalance(updateRequest)
                            .flatMap(updateResponse -> {
                                if ("SUCCESS".equals(updateResponse.getStatus())) {
                                    // Kafka mesajlarını gönder
                                    sendKafkaMessages(balance, usageRequest, updateRequest);
                                    
                                    return Mono.just(createSuccessResponse(usageRequest.getMsisdn(), 
                                                                         "Usage request approved"));
                                } else {
                                    return Mono.just(createFailedResponse(usageRequest.getMsisdn(), 
                                                                        "Failed to update balance"));
                                }
                            });
                })
                .onErrorResume(error -> {
                    logger.error("Error processing usage request for MSISDN: {}, Error: {}", 
                               usageRequest.getMsisdn(), error.getMessage());
                    return Mono.just(createFailedResponse(usageRequest.getMsisdn(), 
                                                        "Internal processing error"));
                });
    }
    
    /**
     * Paket süresi dolmuş mu kontrolü
     */
    private boolean isPackageExpired(String endDate) {
        try {
            LocalDateTime packageEndDate = LocalDateTime.parse(endDate.replace(".0", ""), 
                                                               DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            boolean expired = LocalDateTime.now().isAfter(packageEndDate);
            logger.debug("Package expiry check - End date: {}, Expired: {}", endDate, expired);
            return expired;
        } catch (Exception e) {
            logger.error("Error parsing end date: {}", endDate, e);
            return true; // Güvenlik için expired kabul et
        }
    }
    
    /**
     * Bakiye yeterliliği kontrolü
     */
    private boolean isSufficientBalance(VoltDbBalanceResponse.BalanceResult balance, UsageRequest usageRequest) {
        switch (usageRequest.getUsageType()) {
            case MINUTES:
                return balance.getRemainingMinutes() >= usageRequest.getAmount();
            case SMS:
                return balance.getRemainingSms() >= usageRequest.getAmount();
            case DATA:
                return balance.getRemainingData() >= usageRequest.getAmount();
            default:
                return false;
        }
    }
    
    /**
     * Yeni balance'ları hesapla
     */
    private VoltDbUpdateRequest calculateNewBalance(VoltDbBalanceResponse.BalanceResult balance, 
                                                   UsageRequest usageRequest) {
        Integer newMinutes = balance.getRemainingMinutes();
        Integer newSms = balance.getRemainingSms();
        Integer newData = balance.getRemainingData();
        
        // Kullanım türüne göre ilgili balance'ı azalt
        switch (usageRequest.getUsageType()) {
            case MINUTES:
                newMinutes = balance.getRemainingMinutes() - usageRequest.getAmount();
                break;
            case SMS:
                newSms = balance.getRemainingSms() - usageRequest.getAmount();
                break;
            case DATA:
                newData = balance.getRemainingData() - usageRequest.getAmount();
                break;
        }
        
        logger.debug("Calculated new balance for MSISDN: {} - Minutes: {}, SMS: {}, Data: {}", 
                    usageRequest.getMsisdn(), newMinutes, newSms, newData);
        
        return new VoltDbUpdateRequest(balance.getMsisdn(), newMinutes, newSms, newData);
    }
    
    /**
     * Kafka mesajlarını gönder
     */
    private void sendKafkaMessages(VoltDbBalanceResponse.BalanceResult balance, 
                                  UsageRequest usageRequest, 
                                  VoltDbUpdateRequest updateRequest) {
        
        String timestamp = LocalDateTime.now().format(formatter);
        
        // ABMF mesajı
        AbmfMessage abmfMessage = new AbmfMessage(
                usageRequest.getMsisdn(),
                updateRequest.getRemainingMinutes(),
                updateRequest.getRemainingSms(),
                updateRequest.getRemainingData(),
                timestamp
        );
        
        // CGF mesajı
        CgfMessage cgfMessage = new CgfMessage(
                usageRequest.getMsisdn(),
                usageRequest.getCalledNumber(),
                timestamp,
                usageRequest.getUsageType().getValue(),
                usageRequest.getAmount()
        );
        
        // Notification mesajı (%80 veya %100 kontrolü)
        NotificationMessage notificationMessage = checkNotificationThreshold(balance, updateRequest, usageRequest);
        
        // Kafka'ya gönder
        kafkaProducer.sendAllMessages(abmfMessage, cgfMessage, notificationMessage);
    }
    
    /**
     * Notification threshold kontrolü (%80 veya %100)
     */
    private NotificationMessage checkNotificationThreshold(VoltDbBalanceResponse.BalanceResult balance, 
                                                         VoltDbUpdateRequest updateRequest, 
                                                         UsageRequest usageRequest) {
        
        Integer totalAmount = 0;
        Integer remainingAmount = 0;
        
        // Kullanım türüne göre total ve remaining miktarları al
        switch (usageRequest.getUsageType()) {
            case MINUTES:
                totalAmount = balance.getAmountMinutes();
                remainingAmount = updateRequest.getRemainingMinutes();
                break;
            case SMS:
                totalAmount = balance.getAmountSms();
                remainingAmount = updateRequest.getRemainingSms();
                break;
            case DATA:
                totalAmount = balance.getAmountData();
                remainingAmount = updateRequest.getRemainingData();
                break;
        }
        
        // Kullanım yüzdesini hesapla
        double usagePercentage = ((double) (totalAmount - remainingAmount) / totalAmount) * 100;
        
        // %80 veya %100 kontrolü
        if (usagePercentage >= 100) {
            return createNotificationMessage(balance, updateRequest, usageRequest, 100, "LIMIT_EXCEEDED");
        } else if (usagePercentage >= 80) {
            return createNotificationMessage(balance, updateRequest, usageRequest, 80, "WARNING");
        }
        
        return null; // Threshold'a ulaşmadı
    }
    
    /**
     * Notification mesajı oluştur
     */
    private NotificationMessage createNotificationMessage(VoltDbBalanceResponse.BalanceResult balance, 
                                                        VoltDbUpdateRequest updateRequest, 
                                                        UsageRequest usageRequest, 
                                                        Integer percentage, 
                                                        String message) {
        
        String timestamp = LocalDateTime.now().format(formatter);
        
        return new NotificationMessage(
                usageRequest.getMsisdn(),
                usageRequest.getUsageType().getValue(),
                percentage,
                getRemainingByType(updateRequest, usageRequest.getUsageType()),
                message,
                balance.getPackageName(),
                balance.getStartDate(),
                balance.getEndDate(),
                balance.getAmountMinutes(),
                balance.getAmountSms(),
                balance.getAmountData(),
                updateRequest.getRemainingMinutes(),
                updateRequest.getRemainingSms(),
                updateRequest.getRemainingData(),
                timestamp
        );
    }
    
    /**
     * Kullanım türüne göre kalan miktarı al
     */
    private Integer getRemainingByType(VoltDbUpdateRequest updateRequest, UsageType usageType) {
        switch (usageType) {
            case MINUTES:
                return updateRequest.getRemainingMinutes();
            case SMS:
                return updateRequest.getRemainingSms();
            case DATA:
                return updateRequest.getRemainingData();
            default:
                return 0;
        }
    }
    
    /**
     * Success response oluştur
     */
    private ChfResponse createSuccessResponse(String msisdn, String message) {
        return new ChfResponse("SUCCESS", message, msisdn, LocalDateTime.now().format(formatter));
    }
    
    /**
     * Failed response oluştur
     */
    private ChfResponse createFailedResponse(String msisdn, String message) {
        return new ChfResponse("FAILED", message, msisdn, LocalDateTime.now().format(formatter));
    }
}
