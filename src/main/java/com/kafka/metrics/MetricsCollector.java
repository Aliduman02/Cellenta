package com.kafka.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;

public class MetricsCollector {
    private static final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);
    
    private final AtomicLong totalMessagesProcessed = new AtomicLong(0);
    private final AtomicLong totalMessagesFailed = new AtomicLong(0);
    private final AtomicLong totalMessagesSent = new AtomicLong(0);
    private final AtomicLong totalMessagesReceived = new AtomicLong(0);
    
    private final Map<String, AtomicLong> topicMessageCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    
    private final LocalDateTime startTime = LocalDateTime.now();
    
    public void incrementMessagesProcessed() {
        totalMessagesProcessed.incrementAndGet();
    }
    
    public void incrementMessagesFailed() {
        totalMessagesFailed.incrementAndGet();
    }
    
    public void incrementMessagesSent() {
        totalMessagesSent.incrementAndGet();
    }
    
    public void incrementMessagesReceived() {
        totalMessagesReceived.incrementAndGet();
    }
    
    public void incrementTopicMessageCount(String topic) {
        topicMessageCounts.computeIfAbsent(topic, k -> new AtomicLong()).incrementAndGet();
    }
    
    public void incrementErrorCount(String errorType) {
        errorCounts.computeIfAbsent(errorType, k -> new AtomicLong()).incrementAndGet();
    }
    
    public MetricsSnapshot getMetricsSnapshot() {
        Map<String, Long> topicCounts = new ConcurrentHashMap<>();
        topicMessageCounts.forEach((topic, count) -> topicCounts.put(topic, count.get()));
        
        Map<String, Long> errorCountsSnapshot = new ConcurrentHashMap<>();
        errorCounts.forEach((error, count) -> errorCountsSnapshot.put(error, count.get()));
        
        return new MetricsSnapshot(
            startTime,
            totalMessagesProcessed.get(),
            totalMessagesFailed.get(),
            totalMessagesSent.get(),
            totalMessagesReceived.get(),
            topicCounts,
            errorCountsSnapshot
        );
    }
    
    public void logMetrics() {
        MetricsSnapshot snapshot = getMetricsSnapshot();
        logger.info("=== Metrics Report ===");
        logger.info("Uptime: {} seconds", 
            java.time.Duration.between(snapshot.startTime(), LocalDateTime.now()).getSeconds());
        logger.info("Messages Processed: {}", snapshot.messagesProcessed());
        logger.info("Messages Failed: {}", snapshot.messagesFailed());
        logger.info("Messages Sent: {}", snapshot.messagesSent());
        logger.info("Messages Received: {}", snapshot.messagesReceived());
        logger.info("Success Rate: {}%",
            String.format("%.2f", snapshot.messagesProcessed() > 0 ?
                (double) (snapshot.messagesProcessed() - snapshot.messagesFailed()) / snapshot.messagesProcessed() * 100 : 0.0));
        
        logger.info("Topic Message Counts:");
        snapshot.topicMessageCounts().forEach((topic, count) -> 
            logger.info("  {}: {}", topic, count));
        
        if (!snapshot.errorCounts().isEmpty()) {
            logger.info("Error Counts:");
            snapshot.errorCounts().forEach((error, count) -> 
                logger.info("  {}: {}", error, count));
        }
        logger.info("=====================");
    }
    
    public record MetricsSnapshot(
        LocalDateTime startTime,
        long messagesProcessed,
        long messagesFailed,
        long messagesSent,
        long messagesReceived,
        Map<String, Long> topicMessageCounts,
        Map<String, Long> errorCounts
    ) {}
} 