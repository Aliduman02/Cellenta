package com.i2i.intern.cellenta.chf.client;

import com.i2i.intern.cellenta.chf.config.ApiConfig;
import com.i2i.intern.cellenta.chf.model.VoltDbBalanceRequest;
import com.i2i.intern.cellenta.chf.model.VoltDbBalanceResponse;
import com.i2i.intern.cellenta.chf.model.VoltDbUpdateRequest;
import com.i2i.intern.cellenta.chf.model.VoltDbUpdateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class VoltDbClient {
    
    private static final Logger logger = LoggerFactory.getLogger(VoltDbClient.class);
    
    private final WebClient aggressiveWebClient;
    private final ApiConfig apiConfig;
    
    @Autowired
    public VoltDbClient(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
        this.aggressiveWebClient = createAggressiveWebClient();
        logger.info("🚀 VoltDbClient initialized with AGGRESSIVE optimization");
    }
    
    /**
     * ⚡ AGGRESSIVE WebClient - Max Performance Mode
     */
    private WebClient createAggressiveWebClient() {
        // ⚡ MINIMAL Connection Pool - Fast & Aggressive
        ConnectionProvider connectionProvider = ConnectionProvider.builder("voltdb-aggressive")
                .maxConnections(5)                     // Minimal connections
                .maxIdleTime(Duration.ofSeconds(3))    // Very short idle time
                .maxLifeTime(Duration.ofSeconds(30))   // Short lifetime
                .pendingAcquireTimeout(Duration.ofMillis(50)) // Very short wait
                .pendingAcquireMaxCount(20)            // Small queue
                .evictInBackground(Duration.ofSeconds(5))      // Frequent cleanup
                .build();
        
        // ⚡ AGGRESSIVE HTTP Client - Speed First
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .keepAlive(true)                       
                .compress(false)                       // Disable compression for speed
                .responseTimeout(Duration.ofMillis(400)) // ⚡ AGGRESSIVE: 400ms timeout!
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 200) // ⚡ 200ms connect
                .option(io.netty.channel.ChannelOption.SO_KEEPALIVE, true)
                .option(io.netty.channel.ChannelOption.TCP_NODELAY, true)   
                .option(io.netty.channel.ChannelOption.SO_REUSEADDR, true)
                .option(io.netty.channel.ChannelOption.SO_RCVBUF, 8192)     // Small buffers
                .option(io.netty.channel.ChannelOption.SO_SNDBUF, 8192)     // Small buffers
                .wiretap(false);                       // No logging
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(4 * 1024 * 1024)) // 4MB buffer (smaller)
                .build();
    }
    
    /**
     * ⚡ AGGRESSIVE Balance Request - 400ms timeout
     */
    public Mono<VoltDbBalanceResponse> getBalance(String msisdn) {
        long startTime = System.currentTimeMillis();
        
        // String'i Long'a çevir
        Long msisdnLong = Long.parseLong(msisdn);
        VoltDbBalanceRequest request = new VoltDbBalanceRequest(msisdnLong);
        
        return aggressiveWebClient
                .post()
                .uri(apiConfig.getVoltdb().getFullBalanceUrl())
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    return Mono.error(new RuntimeException("Client error: " + response.statusCode()));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    return Mono.error(new RuntimeException("Server error: " + response.statusCode()));
                })
                .bodyToMono(VoltDbBalanceResponse.class)
                .timeout(Duration.ofMillis(400))       // ⚡ AGGRESSIVE: 400ms hard timeout
                .retryWhen(Retry.fixedDelay(0, Duration.ofMillis(0))) // ⚡ NO RETRY!
                .doOnSuccess(response -> {
                    long duration = System.currentTimeMillis() - startTime;
                    if ("SUCCESS".equals(response.getStatus())) {
                        if (duration > 50) {
                            logger.warn("⚠️ Balance SLOW for MSISDN: {} in {}ms", msisdn, duration);
                        }
                    }
                })
                .doOnError(error -> {
                    long duration = System.currentTimeMillis() - startTime;
                    logger.error("❌ Balance FAILED for MSISDN: {} after {}ms", msisdn, duration);
                });
    }
    
    /**
     * ⚡ AGGRESSIVE Update Request - 400ms timeout
     */
    public Mono<VoltDbUpdateResponse> updateBalance(VoltDbUpdateRequest updateRequest) {
        long startTime = System.currentTimeMillis();
        
        return aggressiveWebClient
                .post()
                .uri(apiConfig.getVoltdb().getFullUpdateUrl())
                .bodyValue(updateRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    return Mono.error(new RuntimeException("Client error: " + response.statusCode()));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    return Mono.error(new RuntimeException("Server error: " + response.statusCode()));
                })
                .bodyToMono(VoltDbUpdateResponse.class)
                .timeout(Duration.ofMillis(400))       // ⚡ AGGRESSIVE: 400ms hard timeout
                .retryWhen(Retry.fixedDelay(0, Duration.ofMillis(0))) // ⚡ NO RETRY!
                .doOnSuccess(response -> {
                    long duration = System.currentTimeMillis() - startTime;
                    if ("SUCCESS".equals(response.getStatus())) {
                        if (duration > 50) {
                            logger.warn("⚠️ Update SLOW for MSISDN: {} in {}ms", updateRequest.getMsisdn(), duration);
                        }
                    }
                })
                .doOnError(error -> {
                    long duration = System.currentTimeMillis() - startTime;
                    logger.error("❌ Update FAILED for MSISDN: {} after {}ms", 
                                updateRequest.getMsisdn(), duration);
                });
    }
    
    /**
     * ⚡ Fast Health Check
     */
    public Mono<String> healthCheck() {
        return aggressiveWebClient
                .get()
                .uri(apiConfig.getVoltdb().getBaseUrl() + "/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(500))
                .onErrorReturn("VoltDB unavailable");
    }
    
    /**
     * ⚡ Connection Pool Stats
     */
    public String getConnectionPoolStats() {
        return "VoltDB AGGRESSIVE Pool: 5 max connections, 400ms timeout, no compression";
    }
}
