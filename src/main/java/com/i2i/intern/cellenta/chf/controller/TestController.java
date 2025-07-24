package com.i2i.intern.cellenta.chf.controller;

import com.i2i.intern.cellenta.chf.client.VoltDbClient;
import com.i2i.intern.cellenta.chf.model.VoltDbBalanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/chf/test")
public class TestController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    private static final ZoneId ISTANBUL_ZONE = ZoneId.of("Europe/Istanbul");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final VoltDbClient voltDbClient;
    
    @Autowired
    public TestController(VoltDbClient voltDbClient) {
        this.voltDbClient = voltDbClient;
    }
    
    /**
     * İstanbul zaman diliminde şu anki zamanı string olarak döndür
     */
    private String getCurrentIstanbulTime() {
        return ZonedDateTime.now(ISTANBUL_ZONE).format(formatter);
    }
    
    /**
     * VoltDB bağlantısını test et
     */
    @GetMapping("/voltdb")
    public Mono<ResponseEntity<String>> testVoltDb() {
        logger.info("Testing VoltDB connection");
        
        return voltDbClient.healthCheck()
                .map(result -> {
                    logger.info("VoltDB test successful: {}", result);
                    return ResponseEntity.ok("VoltDB Test Result at " + getCurrentIstanbulTime() + ": " + result);
                })
                .onErrorResume(error -> {
                    logger.error("VoltDB test failed: {}", error.getMessage());
                    String errorMessage = "VoltDB test failed at " + getCurrentIstanbulTime() + ": " + error.getMessage();
                    return Mono.just(ResponseEntity.status(500).body(errorMessage));
                });
    }
    
    /**
     * Belirli bir MSISDN için balance test et
     */
    @GetMapping("/balance/{msisdn}")
    public Mono<ResponseEntity<Map<String, Object>>> testBalance(@PathVariable String msisdn) {
        logger.info("Testing balance for MSISDN: {}", msisdn);
        
        return voltDbClient.getBalance(msisdn)
                .map(response -> {
                    logger.info("Balance test successful for MSISDN: {}", msisdn);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("status", "SUCCESS");
                    result.put("timestamp", getCurrentIstanbulTime());
                    result.put("msisdn", msisdn);
                    result.put("balance_data", response);
                    result.put("message", "Balance retrieved successfully");
                    
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(error -> {
                    logger.error("Balance test failed for MSISDN: {}, Error: {}", msisdn, error.getMessage());
                    
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "FAILED");
                    errorResult.put("timestamp", getCurrentIstanbulTime());
                    errorResult.put("msisdn", msisdn);
                    errorResult.put("error", error.getMessage());
                    errorResult.put("message", "Balance test failed");
                    
                    return Mono.just(ResponseEntity.status(500).body(errorResult));
                });
    }
    
    /**
     * MSISDN format test endpoint
     */
    @GetMapping("/msisdn/{msisdn}")
    public ResponseEntity<Map<String, Object>> testMsisdnFormat(@PathVariable String msisdn) {
        logger.info("Testing MSISDN format: {}", msisdn);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // MSISDN normalization test
            String cleanMsisdn = msisdn.replaceAll("[^0-9]", "");
            String turkishFormat = "";
            String voltdbFormat = "";
            boolean valid = false;
            
            if (cleanMsisdn.startsWith("90") && cleanMsisdn.length() == 12) {
                // 905079135268 -> 05079135268 (Turkish) / 5079135268 (VoltDB)
                turkishFormat = "0" + cleanMsisdn.substring(2);
                voltdbFormat = cleanMsisdn.substring(2);
                valid = true;
            } else if (cleanMsisdn.startsWith("5") && cleanMsisdn.length() == 10) {
                // 5079135268 -> 05079135268 (Turkish) / 5079135268 (VoltDB)
                turkishFormat = "0" + cleanMsisdn;
                voltdbFormat = cleanMsisdn;
                valid = true;
            } else if (cleanMsisdn.startsWith("05") && cleanMsisdn.length() == 11) {
                // 05079135268 -> 05079135268 (Turkish) / 5079135268 (VoltDB)
                turkishFormat = cleanMsisdn;
                voltdbFormat = cleanMsisdn.substring(1);
                valid = true;
            }
            
            response.put("original", msisdn);
            response.put("cleaned", cleanMsisdn);
            response.put("turkish_format", turkishFormat);
            response.put("voltdb_format", voltdbFormat);
            response.put("international_format", valid ? "+90" + voltdbFormat : "");
            response.put("is_valid", valid);
            response.put("status", valid ? "SUCCESS" : "FAILED");
            response.put("message", valid ? "MSISDN format is valid" : "MSISDN format is invalid");
            response.put("timestamp", getCurrentIstanbulTime()); // İstanbul zaman dilimi
            response.put("test_location", "Istanbul, Turkey");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("MSISDN format test failed for: {}, Error: {}", msisdn, e.getMessage());
            
            response.put("original", msisdn);
            response.put("status", "FAILED");
            response.put("error", e.getMessage());
            response.put("is_valid", false);
            response.put("timestamp", getCurrentIstanbulTime()); // İstanbul zaman dilimi
            response.put("test_location", "Istanbul, Turkey");
            
            return ResponseEntity.status(400).body(response);
        }
    }
    
    /**
     * Basit test endpoint
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        logger.info("Ping test requested");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "CHF Test Service is running!");
        response.put("timestamp", getCurrentIstanbulTime()); // İstanbul zaman dilimi
        response.put("timezone", "Europe/Istanbul");
        response.put("server_location", "Istanbul, Turkey");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Timezone test endpoint - Mevcut timezone'u gösterir
     */
    @GetMapping("/timezone")
    public ResponseEntity<Map<String, Object>> getTimezoneInfo() {
        logger.info("Timezone info requested");
        
        Map<String, Object> response = new HashMap<>();
        response.put("jvm_timezone", java.util.TimeZone.getDefault().getID());
        response.put("jvm_display_name", java.util.TimeZone.getDefault().getDisplayName());
        response.put("current_time_jvm", new java.util.Date().toString());
        response.put("current_time_istanbul", getCurrentIstanbulTime());
        response.put("timestamp", getCurrentIstanbulTime());
        response.put("status", "SUCCESS");
        
        return ResponseEntity.ok(response);
    }
}
