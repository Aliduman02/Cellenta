package com.i2i.intern.cellenta.chf.controller;

import com.i2i.intern.cellenta.chf.model.ChfResponse;
import com.i2i.intern.cellenta.chf.model.UsageRequest;
import com.i2i.intern.cellenta.chf.service.ChfService;
import com.i2i.intern.cellenta.chf.service.TpsCounter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/chf")
public class ChfController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChfController.class);
    private static final ZoneId ISTANBUL_ZONE = ZoneId.of("Europe/Istanbul");
    
    private final ChfService chfService;
    private final TpsCounter tpsCounter;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    public ChfController(ChfService chfService, TpsCounter tpsCounter) {
        this.chfService = chfService;
        this.tpsCounter = tpsCounter;
    }
    
    /**
     * İstanbul zaman diliminde şu anki zamanı string olarak döndür
     */
    private String getCurrentIstanbulTime() {
        return ZonedDateTime.now(ISTANBUL_ZONE).format(formatter);
    }
    
    /**
     * TGF'den gelen kullanım isteğini işle
     * @param usageRequest TGF'den gelen kullanım isteği
     * @return CHF Response
     */
    @PostMapping("/usage")
    public Mono<ResponseEntity<ChfResponse>> processUsage(@Valid @RequestBody UsageRequest usageRequest) {
        
        logger.info("Received usage request from TGF - MSISDN: {}, Type: {}, Amount: {}", 
                   usageRequest.getMsisdn(), usageRequest.getUsageType(), usageRequest.getAmount());
        
        return chfService.processUsageRequest(usageRequest)
                .map(response -> {
                    if ("SUCCESS".equals(response.getStatus())) {
                        logger.info("Usage request processed successfully for MSISDN: {}", 
                                   usageRequest.getMsisdn());
                        return ResponseEntity.ok(response);
                    } else {
                        logger.warn("Usage request failed for MSISDN: {}, Reason: {}", 
                                   usageRequest.getMsisdn(), response.getMessage());
                        // ✅ DÜZELTME: Business logic hataları için de 200 OK dön
                        return ResponseEntity.ok(response);
                    }
                })
                .onErrorResume(error -> {
                    logger.error("Error processing usage request for MSISDN: {}, Error: {}", 
                               usageRequest.getMsisdn(), error.getMessage());
                    
                    ChfResponse errorResponse = new ChfResponse(
                            "FAILED",
                            "Internal server error: " + error.getMessage(),
                            usageRequest.getMsisdn(),
                            getCurrentIstanbulTime() // İstanbul zaman dilimi kullan
                    );
                    
                    // ✅ Sistem hataları için 500 (doğru)
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                  .body(errorResponse));
                });
    }
    
    /**
     * CHF Health Check endpoint
     * @return sistem durumu
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<String>> healthCheck() {
        logger.info("Health check requested");
        
        return Mono.just(ResponseEntity.ok("CHF Service is running - " + getCurrentIstanbulTime()));
    }
    
    /**
     * CHF sistem bilgileri
     * @return sistem bilgileri
     */
    @GetMapping("/info")
    public Mono<ResponseEntity<Object>> getInfo() {
        logger.info("System info requested");
        
        return Mono.just(ResponseEntity.ok(new Object() {
            public final String service = "Charging Function (CHF)";
            public final String version = "1.0.0";
            public final String description = "Cellenta CHF Service for processing usage requests";
            public final String timestamp = getCurrentIstanbulTime(); // İstanbul zaman dilimi kullan
        }));
    }
    
    /**
     * TPS istatistiklerini döndür - REAL-TIME TPS
     * @return TPS detaylı bilgileri
     */
    @GetMapping("/tps")
    public Mono<ResponseEntity<Object>> getTpsStats() {
        logger.info("TPS statistics requested");
        
        TpsCounter.TpsStats stats = tpsCounter.getStats();
        
        return Mono.just(ResponseEntity.ok(new Object() {
            public final String service = "CHF TPS Statistics";
            public final double currentTPS = Math.round(stats.getCurrentTPS() * 100.0) / 100.0;
            public final double last30SecTPS = Math.round(stats.getLast30SecTPS() * 100.0) / 100.0;
            public final double last10SecTPS = Math.round(stats.getLast10SecTPS() * 100.0) / 100.0;
            public final double averageTPS = Math.round(stats.getAverageTPS() * 100.0) / 100.0;
            public final long totalRequests = stats.getTotalRequests();
            public final long successfulRequests = stats.getSuccessfulRequests();
            public final long failedRequests = stats.getFailedRequests();
            public final double successRate = Math.round(stats.getSuccessRate() * 100.0) / 100.0;
            public final long uptimeSeconds = stats.getUptimeSeconds();
            public final String timestamp = getCurrentIstanbulTime();
            public final String timezone = "Europe/Istanbul";
        }));
    }
    
    /**
     * TPS özet bilgileri - Monitoring için
     * @return Kısa TPS özeti
     */
    @GetMapping("/tps/summary")
    public Mono<ResponseEntity<Object>> getTpsSummary() {
        logger.debug("TPS summary requested");
        
        TpsCounter.TpsStats stats = tpsCounter.getStats();
        
        return Mono.just(ResponseEntity.ok(new Object() {
            public final double tps = Math.round(stats.getCurrentTPS() * 100.0) / 100.0;
            public final long total = stats.getTotalRequests();
            public final double success_rate = Math.round(stats.getSuccessRate() * 100.0) / 100.0;
            public final String status = stats.getCurrentTPS() > 10 ? "HIGH" : 
                                        (stats.getCurrentTPS() > 1 ? "MEDIUM" : "LOW");
            public final String timestamp = getCurrentIstanbulTime();
        }));
    }
    
    /**
     * TPS Counter'ı sıfırla - Test/Debug için
     * @return Reset sonucu
     */
    @PostMapping("/tps/reset")
    public Mono<ResponseEntity<Object>> resetTpsCounter() {
        logger.warn("TPS counter reset requested");
        
        tpsCounter.reset();
        
        return Mono.just(ResponseEntity.ok(new Object() {
            public final String message = "TPS counter has been reset";
            public final String timestamp = getCurrentIstanbulTime();
            public final String status = "SUCCESS";
        }));
    }
    
    /**
     * Global Exception Handler
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ChfResponse> handleException(Exception e) {
        logger.error("Unhandled exception occurred: {}", e.getMessage(), e);
        
        ChfResponse errorResponse = new ChfResponse(
                "FAILED",
                "System error: " + e.getMessage(),
                "unknown",
                getCurrentIstanbulTime() // İstanbul zaman dilimi kullan
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
