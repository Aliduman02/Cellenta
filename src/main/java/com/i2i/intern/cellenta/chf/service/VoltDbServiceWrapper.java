package com.i2i.intern.cellenta.chf.service;

import com.i2i.intern.cellenta.chf.client.VoltDbClient;
import com.i2i.intern.cellenta.chf.model.VoltDbBalanceResponse;
import com.i2i.intern.cellenta.chf.model.VoltDbUpdateRequest;
import com.i2i.intern.cellenta.chf.model.VoltDbUpdateResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class VoltDbServiceWrapper {
    
    private static final Logger logger = LoggerFactory.getLogger(VoltDbServiceWrapper.class);
    
    private final VoltDbClient voltDbClient;
    private ReactiveRedisTemplate<String, Object> redisTemplate; // ⚡ FIXED: final kaldırıldı
    
    @Autowired
    public VoltDbServiceWrapper(VoltDbClient voltDbClient,
                               @Autowired(required = false) ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.voltDbClient = voltDbClient;
        this.redisTemplate = redisTemplate; // ⚡ FIXED: Constructor'da set ediliyor
        
        if (redisTemplate != null) {
            logger.info("🔧 VoltDbServiceWrapper initialized with Circuit Breaker and Redis caching");
        } else {
            logger.info("🔧 VoltDbServiceWrapper initialized with Circuit Breaker (Redis not available)");
        }
    }
    
    /**
     * ⚡ CACHED Balance Request with Circuit Breaker
     * Cache TTL: 30 seconds
     */
    @Cacheable(value = "balance", key = "#msisdn", unless = "#result.status != 'SUCCESS'")
    public Mono<VoltDbBalanceResponse> getBalanceWithCache(String msisdn) {
        long startTime = System.currentTimeMillis();
        
        // If Redis is available, use it for distributed caching
        if (redisTemplate != null) {
            return getBalanceWithRedisCache(msisdn, startTime);
        } else {
            // Fallback to direct VoltDB call with circuit breaker
            return getBalanceFromVoltDb(msisdn, startTime);
        }
    }
    
    /**
     * ⚡ Redis-based caching implementation
     */
    private Mono<VoltDbBalanceResponse> getBalanceWithRedisCache(String msisdn, long startTime) {
        String cacheKey = "chf:balance:" + msisdn;
        
        return redisTemplate.opsForValue()
                .get(cacheKey)
                .cast(VoltDbBalanceResponse.class)
                .doOnNext(cached -> {
                    long duration = System.currentTimeMillis() - startTime;
                    logger.info("🎯 Cache HIT for MSISDN: {} in {}ms", msisdn, duration);
                })
                .switchIfEmpty(
                    // Cache MISS - VoltDB'den getir
                    getBalanceFromVoltDb(msisdn, startTime)
                        .doOnNext(response -> {
                            long duration = System.currentTimeMillis() - startTime;
                            logger.info("💾 Cache MISS for MSISDN: {} - fetched from VoltDB in {}ms", msisdn, duration);
                        })
                        .flatMap(response -> {
                            // Cache'e kaydet (30 saniye TTL)
                            if ("SUCCESS".equals(response.getStatus())) {
                                return redisTemplate.opsForValue()
                                    .set(cacheKey, response, Duration.ofSeconds(30))
                                    .thenReturn(response)
                                    .doOnSuccess(cached -> logger.debug("💾 Cached balance for MSISDN: {}", msisdn));
                            }
                            return Mono.just(response);
                        })
                );
    }
    
    /**
     * ⚡ VoltDB Balance Request with Circuit Breaker & Retry
     */
    @CircuitBreaker(name = "voltdb", fallbackMethod = "getBalanceFallback")
    @Retry(name = "voltdb")
    public Mono<VoltDbBalanceResponse> getBalanceFromVoltDb(String msisdn, long startTime) {
        return voltDbClient.getBalance(msisdn)
                .doOnSuccess(response -> {
                    long duration = System.currentTimeMillis() - startTime;
                    logger.debug("🔌 Direct VoltDB call for MSISDN: {} completed in {}ms", msisdn, duration);
                });
    }
    
    /**
     * 🔄 Fallback method for getBalance
     */
    public Mono<VoltDbBalanceResponse> getBalanceFallback(String msisdn, long startTime, Exception ex) {
        logger.error("🚨 Circuit Breaker OPEN for MSISDN: {} - Error: {}", msisdn, ex.getMessage());
        
        // Try to get from cache (even expired) if Redis is available
        if (redisTemplate != null) {
            String cacheKey = "chf:balance:" + msisdn;
            return redisTemplate.opsForValue()
                    .get(cacheKey)
                    .cast(VoltDbBalanceResponse.class)
                    .doOnNext(cached -> logger.warn("⚠️ Using EXPIRED cache for MSISDN: {}", msisdn))
                    .switchIfEmpty(
                        Mono.error(new RuntimeException("VoltDB unavailable and no cached data for MSISDN: " + msisdn))
                    );
        } else {
            // No Redis - return error
            return Mono.error(new RuntimeException("VoltDB unavailable for MSISDN: " + msisdn + " - " + ex.getMessage()));
        }
    }
    
    /**
     * ⚡ Balance Update with Cache Invalidation & Circuit Breaker
     */
    @CircuitBreaker(name = "voltdb", fallbackMethod = "updateBalanceFallback")
    @Retry(name = "voltdb")
    @CacheEvict(value = "balance", key = "#updateRequest.msisdn")
    public Mono<VoltDbUpdateResponse> updateBalance(VoltDbUpdateRequest updateRequest) {
        long startTime = System.currentTimeMillis();
        
        return voltDbClient.updateBalance(updateRequest)
                .doOnSuccess(response -> {
                    long duration = System.currentTimeMillis() - startTime;
                    if ("SUCCESS".equals(response.getStatus())) {
                        // Redis cache'i invalidate et
                        if (redisTemplate != null) {
                            String cacheKey = "chf:balance:" + updateRequest.getMsisdn();
                            redisTemplate.delete(cacheKey)
                                .doOnSuccess(deleted -> logger.debug("🗑️ Redis cache invalidated for MSISDN: {}", updateRequest.getMsisdn()))
                                .subscribe();
                        }
                        logger.info("💾 Balance updated for MSISDN: {} in {}ms", updateRequest.getMsisdn(), duration);
                    }
                });
    }
    
    /**
     * 🔄 Fallback method for updateBalance
     */
    public Mono<VoltDbUpdateResponse> updateBalanceFallback(VoltDbUpdateRequest updateRequest, Exception ex) {
        logger.error("🚨 Circuit Breaker OPEN for update MSISDN: {} - Error: {}", 
                    updateRequest.getMsisdn(), ex.getMessage());
        
        // Fallback: Return failure response
        VoltDbUpdateResponse fallbackResponse = new VoltDbUpdateResponse();
        fallbackResponse.setStatus("FAILED");
        fallbackResponse.setMessage("VoltDB temporarily unavailable - " + ex.getMessage());
        fallbackResponse.setStatusCode(503);
        
        return Mono.just(fallbackResponse);
    }
    
    /**
     * 📊 Get Circuit Breaker Stats (for monitoring)
     */
    public String getCircuitBreakerStats() {
        return "Circuit Breaker: Resilience4j enabled for VoltDB calls";
    }
    
    /**
     * 🧹 Clear cache for specific MSISDN (manual cache invalidation)
     */
    public Mono<Boolean> clearCache(String msisdn) {
        if (redisTemplate != null) {
            String cacheKey = "chf:balance:" + msisdn;
            return redisTemplate.delete(cacheKey)
                    .map(deleted -> deleted > 0)
                    .doOnSuccess(cleared -> logger.info("🧹 Manual cache clear for MSISDN: {} - Success: {}", msisdn, cleared));
        } else {
            logger.warn("🧹 Manual cache clear requested for MSISDN: {} but Redis not available", msisdn);
            return Mono.just(false);
        }
    }
    
    /**
     * 📊 Get cache statistics (for monitoring)
     */
    public String getCacheStats() {
        if (redisTemplate != null) {
            return "Redis caching enabled - TTL: 30 seconds";
        } else {
            return "In-memory caching only - Redis not available";
        }
    }
}
