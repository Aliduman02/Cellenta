package com.i2i.intern.cellenta.chf.controller;

import com.i2i.intern.cellenta.chf.client.VoltDbClient;
import com.i2i.intern.cellenta.chf.model.VoltDbBalanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/chf/test")
public class TestController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    private final VoltDbClient voltDbClient;
    
    @Autowired
    public TestController(VoltDbClient voltDbClient) {
        this.voltDbClient = voltDbClient;
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
                    return ResponseEntity.ok("VoltDB Test Result: " + result);
                })
                .onErrorResume(error -> {
                    logger.error("VoltDB test failed: {}", error.getMessage());
                    String errorMessage = "VoltDB test failed: " + error.getMessage();
                    return Mono.just(ResponseEntity.status(500).body(errorMessage));
                });
    }
    
    /**
     * Belirli bir MSISDN için balance test et
     */
    @GetMapping("/balance/{msisdn}")
    public Mono<ResponseEntity<VoltDbBalanceResponse>> testBalance(@PathVariable String msisdn) {
        logger.info("Testing balance for MSISDN: {}", msisdn);
        
        return voltDbClient.getBalance(msisdn)
                .map(response -> {
                    logger.info("Balance test successful for MSISDN: {}", msisdn);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(error -> {
                    logger.error("Balance test failed for MSISDN: {}, Error: {}", msisdn, error.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(null));
                });
    }
    
    /**
     * Basit test endpoint
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        logger.info("Ping test requested");
        return ResponseEntity.ok("CHF Test Service is running!");
    }
}
