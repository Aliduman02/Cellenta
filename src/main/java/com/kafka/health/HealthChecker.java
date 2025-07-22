package com.kafka.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class HealthChecker {
    private static final Logger logger = LoggerFactory.getLogger(HealthChecker.class);
    
    private final AtomicLong messagesProcessed = new AtomicLong(0);
    private final AtomicLong messagesFailed = new AtomicLong(0);
    private final LocalDateTime startTime = LocalDateTime.now();
    
    public void incrementMessagesProcessed() {
        messagesProcessed.incrementAndGet();
    }
    
    public void incrementMessagesFailed() {
        messagesFailed.incrementAndGet();
    }
    
    public HealthStatus getHealthStatus() {
        long totalMessages = messagesProcessed.get() + messagesFailed.get();
        double successRate = totalMessages > 0 ? (double) messagesProcessed.get() / totalMessages * 100 : 100.0;
        
        return new HealthStatus(
            "UP",
            startTime,
            messagesProcessed.get(),
            messagesFailed.get(),
            successRate
        );
    }
    
    public void logHealthStatus() {
        HealthStatus status = getHealthStatus();
        logger.info("Health Check - Status: {}, Messages: {}/{}, Success Rate: {}%",
            status.status(),
            status.messagesProcessed(),
            status.messagesProcessed() + status.messagesFailed(),
            String.format("%.2f", status.successRate()));
    }
    
    public record HealthStatus(
        String status,
        LocalDateTime startTime,
        long messagesProcessed,
        long messagesFailed,
        double successRate
    ) {}
} 