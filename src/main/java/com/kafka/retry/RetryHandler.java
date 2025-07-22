package com.kafka.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

public class RetryHandler {
    private static final Logger logger = LoggerFactory.getLogger(RetryHandler.class);
    
    private final int maxRetries;
    private final long delayMs;
    
    public RetryHandler(int maxRetries, long delayMs) {
        this.maxRetries = maxRetries;
        this.delayMs = delayMs;
    }
    
    public <T> T executeWithRetry(RetryableOperation<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                logger.warn("Attempt {} failed: {}", attempt, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(delayMs * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        
        logger.error("All {} retry attempts failed", maxRetries);
        throw lastException;
    }
    
    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }
} 