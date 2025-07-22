package com.kafka.circuit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CircuitBreaker {
    private static final Logger logger = LoggerFactory.getLogger(CircuitBreaker.class);
    
    private final String name;
    private final int failureThreshold;
    private final Duration timeout;
    
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicReference<Instant> lastFailureTime = new AtomicReference<>();
    
    public CircuitBreaker(String name, int failureThreshold, Duration timeout) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.timeout = timeout;
    }
    
    public <T> T execute(CircuitBreakerOperation<T> operation) throws Exception {
        State currentState = state.get();
        
        if (currentState == State.OPEN) {
            if (shouldAttemptReset()) {
                logger.info("Circuit breaker {} attempting to reset", name);
                state.compareAndSet(State.OPEN, State.HALF_OPEN);
            } else {
                throw new CircuitBreakerOpenException("Circuit breaker " + name + " is OPEN");
            }
        }
        
        try {
            T result = operation.execute();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }
    
    private void onSuccess() {
        failureCount.set(0);
        state.set(State.CLOSED);
        logger.debug("Circuit breaker {} operation succeeded", name);
    }
    
    private void onFailure() {
        lastFailureTime.set(Instant.now());
        int failures = failureCount.incrementAndGet();
        
        if (failures >= failureThreshold) {
            state.set(State.OPEN);
            logger.warn("Circuit breaker {} opened after {} failures", name, failures);
        }
    }
    
    private boolean shouldAttemptReset() {
        Instant lastFailure = lastFailureTime.get();
        return lastFailure != null && 
               Instant.now().isAfter(lastFailure.plus(timeout));
    }
    
    public State getState() {
        return state.get();
    }
    
    public int getFailureCount() {
        return failureCount.get();
    }
    
    public enum State {
        CLOSED,     // Normal operation
        OPEN,       // Failing, reject requests
        HALF_OPEN   // Testing if service recovered
    }
    
    @FunctionalInterface
    public interface CircuitBreakerOperation<T> {
        T execute() throws Exception;
    }
    
    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
} 