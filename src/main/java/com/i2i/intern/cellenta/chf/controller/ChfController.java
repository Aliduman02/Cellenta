package com.i2i.intern.cellenta.chf.controller;

import com.i2i.intern.cellenta.chf.model.ChfResponse;
import com.i2i.intern.cellenta.chf.model.UsageRequest;
import com.i2i.intern.cellenta.chf.service.ChfService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/chf")
public class ChfController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChfController.class);
    
    private final ChfService chfService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    public ChfController(ChfService chfService) {
        this.chfService = chfService;
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
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                })
                .onErrorResume(error -> {
                    logger.error("Error processing usage request for MSISDN: {}, Error: {}", 
                               usageRequest.getMsisdn(), error.getMessage());
                    
                    ChfResponse errorResponse = new ChfResponse(
                            "FAILED",
                            "Internal server error: " + error.getMessage(),
                            usageRequest.getMsisdn(),
                            LocalDateTime.now().format(formatter)
                    );
                    
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
        
        return Mono.just(ResponseEntity.ok("CHF Service is running - " + 
                                         LocalDateTime.now().format(formatter)));
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
            public final String timestamp = LocalDateTime.now().format(formatter);
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
                LocalDateTime.now().format(formatter)
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
