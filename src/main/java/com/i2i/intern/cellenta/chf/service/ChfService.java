package com.i2i.intern.cellenta.chf.service;

import com.i2i.intern.cellenta.chf.kafka.KafkaProducer;
import com.i2i.intern.cellenta.chf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ChfService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChfService.class);
    private static final ZoneId ISTANBUL_ZONE = ZoneId.of("Europe/Istanbul");
    
    // ⚡ CHANGED: VoltDbClient -> VoltDbServiceWrapper
    private final VoltDbServiceWrapper voltDbService;
    private final KafkaProducer kafkaProducer;
    private final TpsCounter tpsCounter;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    public ChfService(VoltDbServiceWrapper voltDbService, KafkaProducer kafkaProducer, TpsCounter tpsCounter) {
        this.voltDbService = voltDbService;  // ⚡ UPDATED
        this.kafkaProducer = kafkaProducer;
        this.tpsCounter = tpsCounter;
        logger.info("🚀 ChfService initialized with optimized VoltDB wrapper and circuit breaker");
    }
    
    /**
     * İstanbul zaman diliminde şu anki zamanı string olarak döndür
     */
    private String getCurrentIstanbulTime() {
        return ZonedDateTime.now(ISTANBUL_ZONE).format(formatter);
    }
    
    /**
     * ⚡ OPTIMIZED - Ana CHF işlemi - TGF'den gelen kullanım isteğini işle
     * Circuit Breaker & Cache enabled
     * @param usageRequest TGF'den gelen kullanım isteği
     * @return CHF Response (SUCCESS/FAILED)
     */
    public Mono<ChfResponse> processUsageRequest(UsageRequest usageRequest) {
        long startTime = System.currentTimeMillis();  // ⚡ Performance tracking
        
        // ✅ TPS Counter: Request geldiğini kaydet
        tpsCounter.recordRequest();
        
        logger.info("⚡ Processing usage request for MSISDN: {}, Type: {}, Amount: {}", 
                   usageRequest.getMsisdn(), usageRequest.getUsageType(), usageRequest.getAmount());
        
        String msisdn = usageRequest.getMsisdn();
        logger.debug("Using MSISDN for VoltDB: {}", msisdn);
        
        // ⚡ OPTIMIZED: Cache-enabled balance retrieval with circuit breaker
        return voltDbService.getBalanceWithCache(msisdn)
                .flatMap(balanceResponse -> validateAndProcessRequest(balanceResponse, usageRequest))
                .onErrorResume(error -> handleError(error, usageRequest))
                .doOnNext(response -> {
                    long duration = System.currentTimeMillis() - startTime;
                    
                    // ✅ TPS Counter: Success/Failure durumunu kaydet
                    if ("SUCCESS".equals(response.getStatus())) {
                        tpsCounter.recordSuccess();
                        logger.info("✅ Request completed successfully for MSISDN: {} in {}ms", 
                                   usageRequest.getMsisdn(), duration);
                    } else {
                        tpsCounter.recordFailure();
                        logger.warn("⚠️ Request failed for MSISDN: {} in {}ms - Reason: {}", 
                                   usageRequest.getMsisdn(), duration, response.getMessage());
                    }
                });
    }
    
    /**
     * Balance response'ını validate et ve işlemi başlat
     */
 private Mono<ChfResponse> validateAndProcessRequest(VoltDbBalanceResponse balanceResponse, 
                                                       UsageRequest usageRequest) {
        // Balance response validasyonu
        if (!isValidBalanceResponse(balanceResponse)) {
            return Mono.just(createFailedResponse(usageRequest.getMsisdn(), "Balance information not found"));
        }
        
        VoltDbBalanceResponse.BalanceResult balance = balanceResponse.getResults().get(0);
        
        // Business validasyonları
        if (isPackageExpired(balance.getEndDate())) {
            return Mono.just(createFailedResponse(usageRequest.getMsisdn(), "Package expired"));
        }
        
        if (!isSufficientBalance(balance, usageRequest)) {
            return Mono.just(createFailedResponse(usageRequest.getMsisdn(), "Insufficient balance"));
        }
        
        // Başarılı işlem akışı
        return processSuccessfulUsage(balance, usageRequest);
    }
    
    /**
     * ⚡ OPTIMIZED - Başarılı kullanım işlemini gerçekleştir
     */
    private Mono<ChfResponse> processSuccessfulUsage(VoltDbBalanceResponse.BalanceResult balance, 
                                                    UsageRequest usageRequest) {
        VoltDbUpdateRequest updateRequest = calculateNewBalance(balance, usageRequest);
        
        // ⚡ OPTIMIZED: Circuit breaker enabled update
        return voltDbService.updateBalance(updateRequest)
                .flatMap(updateResponse -> handleVoltDbUpdateResponse(updateResponse, balance, usageRequest, updateRequest));
    }
    
    /**
     * VoltDB güncelleme response'ını işle - FAST RESPONSE
     */
    private Mono<ChfResponse> handleVoltDbUpdateResponse(VoltDbUpdateResponse updateResponse,
                                                        VoltDbBalanceResponse.BalanceResult balance,
                                                        UsageRequest usageRequest,
                                                        VoltDbUpdateRequest updateRequest) {
        if ("SUCCESS".equals(updateResponse.getStatus())) {
            // ✅ Response'u hemen döndür - Kafka'yı arka planda gönder
            ChfResponse successResponse = createSuccessResponse(usageRequest.getMsisdn(), "Usage request approved");
            
            // Kafka mesajlarını ASYNC olarak gönder (response'u bekletmez)
            sendKafkaMessages(balance, usageRequest, updateRequest);
            
            return Mono.just(successResponse);
        } else {
            return Mono.just(createFailedResponse(usageRequest.getMsisdn(), "Failed to update balance"));
        }
    }
    
    /**
     * Balance response validasyonu
     */
    private boolean isValidBalanceResponse(VoltDbBalanceResponse balanceResponse) {
        return "SUCCESS".equals(balanceResponse.getStatus()) && 
               balanceResponse.getResults() != null && 
               !balanceResponse.getResults().isEmpty();
    }
    
    /**
     * Error handling - ⚡ ENHANCED with Circuit Breaker awareness
     */
    private Mono<ChfResponse> handleError(Throwable error, UsageRequest usageRequest) {
        String errorMessage = error.getMessage();
        
        // Circuit breaker durumunu kontrol et
        if (errorMessage != null && errorMessage.contains("Circuit Breaker")) {
            logger.error("🚨 Circuit Breaker triggered for MSISDN: {}, Error: {}", 
                       usageRequest.getMsisdn(), errorMessage);
            return Mono.just(createFailedResponse(usageRequest.getMsisdn(), "Service temporarily unavailable"));
        } else {
            logger.error("❌ Error processing usage request for MSISDN: {}, Error: {}", 
                       usageRequest.getMsisdn(), errorMessage);
            return Mono.just(createFailedResponse(usageRequest.getMsisdn(), "Internal processing error"));
        }
    }
    
    /**
     * Paket süresi dolmuş mu kontrolü - DateObject desteği ile - İstanbul zaman dilimi
     */
  private boolean isPackageExpired(Object endDate) {
        try {
            if (endDate == null) {
                logger.warn("End date is null, considering as not expired");
                return false; // null ise expired değil kabul et
            }
            
            logger.debug("End date received: {}", endDate);
            
            // DateObject geliyorsa time field'ını çıkar ve parse et
            if (endDate.toString().contains("DateObject")) {
                logger.debug("Processing DateObject: {}", endDate);
                
                String endDateStr = endDate.toString();
                // DateObject{usec=0, time=1755437572332000} formatından time'ı çıkar
                if (endDateStr.contains("time=")) {
                    try {
                        String timeStr = endDateStr.substring(endDateStr.indexOf("time=") + 5);
                        timeStr = timeStr.substring(0, timeStr.indexOf("}"));
                        
                        // Microsecond'dan millisecond'a çevir
                        long timeMicros = Long.parseLong(timeStr);
                        long timeMillis = timeMicros / 1000; // Microsecond'dan millisecond'a
                        
                        // İstanbul zaman diliminde karşılaştır
                        ZonedDateTime packageEndDate = ZonedDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(timeMillis),
                            ISTANBUL_ZONE
                        );
                        
                        ZonedDateTime currentIstanbulTime = ZonedDateTime.now(ISTANBUL_ZONE);
                        boolean expired = currentIstanbulTime.isAfter(packageEndDate);
                        logger.debug("Package expiry check (Istanbul time) - End date: {}, Current: {}, Expired: {}", 
                                   packageEndDate, currentIstanbulTime, expired);
                        return expired;
                        
                    } catch (Exception e) {
                        logger.warn("Could not parse DateObject time field: {}, assuming not expired", endDate);
                        return false;
                    }
                } else {
                    logger.warn("DateObject does not contain time field: {}, assuming not expired", endDate);
                    return false;
                }
            }
            
            // String olarak gelirse eski yöntemi kullan - İstanbul zaman dilimi ile
            String endDateStr = endDate.toString();
            if (endDateStr.isEmpty()) {
                logger.warn("End date is empty, considering as not expired");
                return false;
            }
            
            String cleanEndDate = endDateStr.replace(".0", "");
            LocalDateTime packageEndLocalTime = LocalDateTime.parse(cleanEndDate, 
                                                               DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // LocalDateTime'ı İstanbul zaman diliminde ZonedDateTime'a çevir
            ZonedDateTime packageEndDate = packageEndLocalTime.atZone(ISTANBUL_ZONE);
            ZonedDateTime currentIstanbulTime = ZonedDateTime.now(ISTANBUL_ZONE);
            
            boolean expired = currentIstanbulTime.isAfter(packageEndDate);
            logger.debug("Package expiry check (Istanbul time) - End date: {}, Current: {}, Expired: {}", 
                       packageEndDate, currentIstanbulTime, expired);
            return expired;
            
        } catch (Exception e) {
            logger.warn("Could not parse end date: {}, assuming not expired. Error: {}", endDate, e.getMessage());
            return false; // Parse edilmiyorsa expired değil kabul et
        }
    }

    /**
     * Bakiye yeterliliği kontrolü - Utility metodu kullanarak
     */
private boolean isSufficientBalance(VoltDbBalanceResponse.BalanceResult balance, UsageRequest usageRequest) {
        Integer currentRemaining = getCurrentRemainingByType(balance, usageRequest.getUsageType());
        return currentRemaining >= usageRequest.getAmount();
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
        
        // MSISDN'i Long'a çevir
        String msisdnStr = usageRequest.getMsisdn();
        Long msisdnLong = Long.parseLong(msisdnStr);
        
        return new VoltDbUpdateRequest(msisdnLong, newMinutes, newSms, newData);
    }
    
    /**
     * Kafka mesajlarını asenkron olarak gönder - FIRE AND FORGET - İstanbul zaman dilimi ile
     */
    @Async("kafkaTaskExecutor")
    public void sendKafkaMessages(VoltDbBalanceResponse.BalanceResult balance, 
                                  UsageRequest usageRequest, 
                                  VoltDbUpdateRequest updateRequest) {
        
        String timestamp = getCurrentIstanbulTime(); // İstanbul zaman dilimi kullan
        
        // Kafka mesajları için MSISDN kullan
        String msisdnForKafka = usageRequest.getMsisdn();
        String calledNumberForKafka = usageRequest.getCalledNumber();
        
        logger.debug("Sending Kafka messages for MSISDN: {} with Istanbul timestamp: {}", 
                    msisdnForKafka, timestamp);
        
        // ABMF mesajı
        AbmfMessage abmfMessage = new AbmfMessage(
                msisdnForKafka,
                updateRequest.getRemainingMinutes(),
                updateRequest.getRemainingSms(),
                updateRequest.getRemainingData(),
                timestamp
        );
        
        // CGF mesajı
        CgfMessage cgfMessage = new CgfMessage(
                msisdnForKafka,
                calledNumberForKafka,
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
        
        // Utility metodları ile temiz kod
        Integer totalAmount = getTotalAmountByType(balance, usageRequest.getUsageType());
        Integer currentRemainingAmount = getCurrentRemainingByType(balance, usageRequest.getUsageType());
        Integer newRemainingAmount = getNewRemainingByType(updateRequest, usageRequest.getUsageType());
        
        // Önceki ve sonraki kullanım yüzdelerini hesapla
        double previousUsagePercentage = ((double) (totalAmount - currentRemainingAmount) / totalAmount) * 100;
        double currentUsagePercentage = ((double) (totalAmount - newRemainingAmount) / totalAmount) * 100;
        
        logger.debug("Threshold check for MSISDN: {} - Previous: {}%, Current: {}%", 
                    usageRequest.getMsisdn(), 
                    String.format("%.2f", previousUsagePercentage), 
                    String.format("%.2f", currentUsagePercentage));
        
        // Threshold geçiş kontrolü - Önce 100%, sonra 80%
        boolean crossed100 = previousUsagePercentage < 100 && currentUsagePercentage >= 100;
        boolean crossed80 = previousUsagePercentage < 80 && currentUsagePercentage >= 80;
        
        if (crossed100) {
            logger.info("MSISDN: {} crossed 100% threshold ({}% -> {}%)", 
                       usageRequest.getMsisdn(), 
                       String.format("%.2f", previousUsagePercentage), 
                       String.format("%.2f", currentUsagePercentage));
            return createNotificationMessage(balance, updateRequest, usageRequest, 100, "LIMIT_EXCEEDED");
        } else if (crossed80) {
            logger.info("MSISDN: {} crossed 80% threshold ({}% -> {}%)", 
                       usageRequest.getMsisdn(), 
                       String.format("%.2f", previousUsagePercentage), 
                       String.format("%.2f", currentUsagePercentage));
            return createNotificationMessage(balance, updateRequest, usageRequest, 80, "WARNING");
        }
        
        logger.debug("No threshold crossed for MSISDN: {} - no notification sent", usageRequest.getMsisdn());
        return null; // Threshold geçişi yok
    }
    
    /**
     * Notification mesajı oluştur - İstanbul zaman dilimi ile
     */
    private NotificationMessage createNotificationMessage(VoltDbBalanceResponse.BalanceResult balance, 
                                                        VoltDbUpdateRequest updateRequest, 
                                                        UsageRequest usageRequest, 
                                                        Integer percentage, 
                                                        String message) {
        
        String timestamp = getCurrentIstanbulTime(); // İstanbul zaman dilimi kullan
        
        // Notification için MSISDN kullan
        String msisdnForNotification = usageRequest.getMsisdn();
        
        // Date object'lerini doğru formatta String'e çevir
        String startDateStr = balance.getStartDate() != null ? balance.getStartDate().toDateString() : "";
        String endDateStr = balance.getEndDate() != null ? balance.getEndDate().toDateString() : "";
        
        return new NotificationMessage(
                msisdnForNotification,
                usageRequest.getUsageType().getValue(),
                percentage,
                getNewRemainingByType(updateRequest, usageRequest.getUsageType()),
                message,
                balance.getPackageName(),
                startDateStr,
                endDateStr,
                balance.getAmountMinutes(),
                balance.getAmountSms(),
                balance.getAmountData(),
                updateRequest.getRemainingMinutes(),
                updateRequest.getRemainingSms(),
                updateRequest.getRemainingData(),
                timestamp
        );
    }
    
    // ===========================================
    // UTILITY METODLARI - Switch-case tekrarını çözer
    // ===========================================
    
    /**
     * Kullanım türüne göre toplam paketteki miktarı al
     */
    private Integer getTotalAmountByType(VoltDbBalanceResponse.BalanceResult balance, UsageType usageType) {
        switch (usageType) {
            case MINUTES:
                return balance.getAmountMinutes();
            case SMS:
                return balance.getAmountSms();
            case DATA:
                return balance.getAmountData();
            default:
                return 0;
        }
    }
    
    /**
     * Kullanım türüne göre mevcut kalan miktarı al (usage ÖNCESI)
     */
private Integer getCurrentRemainingByType(VoltDbBalanceResponse.BalanceResult balance, UsageType usageType) {
        switch (usageType) {
            case MINUTES:
                return balance.getRemainingMinutes();
            case SMS:
                return balance.getRemainingSms();
            case DATA:
                return balance.getRemainingData();
            default:
                return 0;
        }
    }
    
    /**
     * Kullanım türüne göre yeni kalan miktarı al (usage SONRASI)
     */
    private Integer getNewRemainingByType(VoltDbUpdateRequest updateRequest, UsageType usageType) {
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
     * Success response oluştur - İstanbul zaman dilimi ile
     */
    private ChfResponse createSuccessResponse(String msisdn, String message) {
        return new ChfResponse("SUCCESS", message, msisdn, getCurrentIstanbulTime());
    }
    
    /**
     * Failed response oluştur - İstanbul zaman dilimi ile
     */
    private ChfResponse createFailedResponse(String msisdn, String message) {
        return new ChfResponse("FAILED", message, msisdn, getCurrentIstanbulTime());
    }
    
    /**
     * ⚡ NEW: Get service statistics for monitoring
     */
    public String getServiceStats() {
        return String.format(
            "ChfService Stats - VoltDB: %s, Cache: %s, CircuitBreaker: %s",
            "Optimized connection pool",
            voltDbService.getCacheStats(),
            voltDbService.getCircuitBreakerStats()
        );
    }
}
