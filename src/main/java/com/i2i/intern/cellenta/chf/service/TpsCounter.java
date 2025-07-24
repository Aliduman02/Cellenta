package com.i2i.intern.cellenta.chf.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TpsCounter {
    
    private static final ZoneId ISTANBUL_ZONE = ZoneId.of("Europe/Istanbul");
    
    // Thread-safe request counter
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    
    // Sliding window için request timestamp'leri (son 60 saniye)
    private final ConcurrentLinkedQueue<Long> requestTimestamps = new ConcurrentLinkedQueue<>();
    
    // Başlangıç zamanı
    private final long startTime = System.currentTimeMillis();
    
    /**
     * Yeni request geldiğinde çağır
     */
    public void recordRequest() {
        long currentTime = System.currentTimeMillis();
        totalRequests.incrementAndGet();
        requestTimestamps.offer(currentTime);
        
        // 60 saniyeden eski timestamp'leri temizle
        cleanOldTimestamps(currentTime);
    }
    
    /**
     * Başarılı request'i kaydet
     */
    public void recordSuccess() {
        successfulRequests.incrementAndGet();
    }
    
    /**
     * Başarısız request'i kaydet
     */
    public void recordFailure() {
        failedRequests.incrementAndGet();
    }
    
    /**
     * Son 60 saniyedeki TPS hesapla
     */
    public double getCurrentTPS() {
        long currentTime = System.currentTimeMillis();
        cleanOldTimestamps(currentTime);
        
        int recentRequests = requestTimestamps.size();
        
        // Son 60 saniyedeki request sayısını 60'a böl
        return recentRequests / 60.0;
    }
    
    /**
     * Son N saniyedeki TPS hesapla
     */
    public double getTPS(int seconds) {
        long currentTime = System.currentTimeMillis();
        long cutoffTime = currentTime - (seconds * 1000L);
        
        int recentRequests = (int) requestTimestamps.stream()
                .filter(timestamp -> timestamp >= cutoffTime)
                .count();
        
        return recentRequests / (double) seconds;
    }
    
    /**
     * Toplam istatistikler
     */
    public TpsStats getStats() {
        long currentTime = System.currentTimeMillis();
        cleanOldTimestamps(currentTime);
        
        long total = totalRequests.get();
        long successful = successfulRequests.get();
        long failed = failedRequests.get();
        double upTimeSeconds = (currentTime - startTime) / 1000.0;
        double averageTPS = total / Math.max(upTimeSeconds, 1.0);
        double currentTPS = getCurrentTPS();
        double last30SecTPS = getTPS(30);
        double last10SecTPS = getTPS(10);
        
        return new TpsStats(
                total,
                successful, 
                failed,
                averageTPS,
                currentTPS,
                last30SecTPS,
                last10SecTPS,
                ZonedDateTime.now(ISTANBUL_ZONE),
                (long) upTimeSeconds
        );
    }
    
    /**
     * 60 saniyeden eski timestamp'leri temizle
     */
    private void cleanOldTimestamps(long currentTime) {
        long cutoffTime = currentTime - 60000; // 60 saniye
        
        while (!requestTimestamps.isEmpty() && requestTimestamps.peek() < cutoffTime) {
            requestTimestamps.poll();
        }
    }
    
    /**
     * Counter'ları sıfırla (test için)
     */
    public void reset() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        requestTimestamps.clear();
    }
    
    /**
     * TPS İstatistikleri Class'ı
     */
    public static class TpsStats {
        private final long totalRequests;
        private final long successfulRequests;
        private final long failedRequests;
        private final double averageTPS;
        private final double currentTPS;
        private final double last30SecTPS;
        private final double last10SecTPS;
        private final ZonedDateTime timestamp;
        private final long uptimeSeconds;
        
        public TpsStats(long totalRequests, long successfulRequests, long failedRequests,
                       double averageTPS, double currentTPS, double last30SecTPS, double last10SecTPS,
                       ZonedDateTime timestamp, long uptimeSeconds) {
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.averageTPS = averageTPS;
            this.currentTPS = currentTPS;
            this.last30SecTPS = last30SecTPS;
            this.last10SecTPS = last10SecTPS;
            this.timestamp = timestamp;
            this.uptimeSeconds = uptimeSeconds;
        }
        
        // Getters
        public long getTotalRequests() { return totalRequests; }
        public long getSuccessfulRequests() { return successfulRequests; }
        public long getFailedRequests() { return failedRequests; }
        public double getAverageTPS() { return averageTPS; }
        public double getCurrentTPS() { return currentTPS; }
        public double getLast30SecTPS() { return last30SecTPS; }
        public double getLast10SecTPS() { return last10SecTPS; }
        public ZonedDateTime getTimestamp() { return timestamp; }
        public long getUptimeSeconds() { return uptimeSeconds; }
        
        public double getSuccessRate() {
            return totalRequests > 0 ? (successfulRequests * 100.0) / totalRequests : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "TPS Stats: Current=%.2f, 30s=%.2f, 10s=%.2f, Avg=%.2f | " +
                "Total=%d, Success=%d (%.1f%%), Failed=%d | Uptime=%ds",
                currentTPS, last30SecTPS, last10SecTPS, averageTPS,
                totalRequests, successfulRequests, getSuccessRate(), failedRequests, uptimeSeconds
            );
        }
    }
}
