package com.i2i.intern.cellenta.chf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Value("${chf.async.core-pool-size:25}")
    private int corePoolSize;
    
    @Value("${chf.async.max-pool-size:100}")
    private int maxPoolSize;
    
    @Value("${chf.async.queue-capacity:500}")
    private int queueCapacity;
    
    @Value("${chf.async.thread-name-prefix:chf-async-}")
    private String threadNamePrefix;
    
    /**
     * Ana Async Task Executor - CHF işlemleri için
     */
    @Bean("chfTaskExecutor")
    public Executor chfTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        
        // Thread pool davranışı
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * VoltDB işlemleri için özel thread pool
     */
    @Bean("voltDbTaskExecutor")
    public Executor voltDbTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(15);  // VoltDB için daha az thread
        executor.setMaxPoolSize(50);   // VoltDB connection limit'e göre ayarlandı
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("voltdb-task-");
        
        // VoltDB için optimizasyon
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(30);
        executor.setAllowCoreThreadTimeOut(false);  // VoltDB için core thread'leri koru
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Kafka mesajları için özel thread pool
     */
    @Bean("kafkaTaskExecutor")
    public Executor kafkaTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);  // Kafka için orta seviye
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(300);
        executor.setThreadNamePrefix("kafka-task-");
        
        // Kafka için optimizasyon
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy()); // Kafka fail-fast
        executor.setKeepAliveSeconds(45);
        
        executor.initialize();  
        return executor;
    }
    
    /**
     * Scheduled task'lar için thread pool (monitoring, cleanup vs.)
     */
    @Bean("scheduledTaskExecutor")
    public ThreadPoolTaskScheduler scheduledTaskExecutor() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);  // Scheduled task'lar için az thread yeterli
        scheduler.setThreadNamePrefix("chf-scheduled-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(20);
        
        scheduler.initialize();
        return scheduler;
    }
    
    /**
     * Default async executor - Spring @Async için
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        return chfTaskExecutor(); // Ana executor'ı default olarak kullan
    }
}
