package com.i2i.intern.cellenta.chf.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import io.netty.channel.ChannelOption;  // ✅ DÜZELTME: Doğru import

import java.time.Duration;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class WebClientConfig {
    
    private static final ZoneId ISTANBUL_ZONE = ZoneId.of("Europe/Istanbul");
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setTimeZone(TimeZone.getTimeZone(ISTANBUL_ZONE));
        return mapper;
    }
    
    /**
     * VoltDB için optimize edilmiş connection pool
     */
    @Bean("voltDbConnectionProvider")
    public ConnectionProvider voltDbConnectionProvider() {
        return ConnectionProvider.builder("voltdb-pool")
                .maxConnections(20)              // Maksimum connection sayısı
                .pendingAcquireMaxCount(50)      // Bekleyen request queue boyutu
                .pendingAcquireTimeout(Duration.ofSeconds(30))  // Connection bekleme timeout
                .maxIdleTime(Duration.ofSeconds(30))            // Idle connection timeout
                .maxLifeTime(Duration.ofMinutes(5))             // Connection max yaşam süresi
                .evictInBackground(Duration.ofSeconds(10))      // Background cleanup
                .build();
    }
    
    /**
     * VoltDB için yüksek performanslı HTTP Client
     */
    @Bean("voltDbHttpClient")
    public HttpClient voltDbHttpClient(ConnectionProvider voltDbConnectionProvider) {
        return HttpClient.create(voltDbConnectionProvider)
                .responseTimeout(Duration.ofSeconds(30))        // Response timeout
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)  // ✅ DÜZELTME: Doğru ChannelOption
                .option(ChannelOption.SO_KEEPALIVE, true)    // ✅ DÜZELTME: Keep-alive
                .option(ChannelOption.TCP_NODELAY, true)     // ✅ DÜZELTME: TCP No-delay (düşük latency)
                .compress(true)                                 // Response compression
                .followRedirect(false);                         // Redirect takip etme (performance)
    }
    
    /**
     * Ana WebClient - VoltDB için optimize edilmiş
     */
    @Bean
    public WebClient webClient(ObjectMapper objectMapper, HttpClient voltDbHttpClient) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(voltDbHttpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> {
                            // Buffer boyutunu artır (large responses için)
                            configurer.defaultCodecs().maxInMemorySize(32 * 1024 * 1024); // 32MB buffer
                            configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                            configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
                        })
                        .build())
                .build();
    }
    
    /**
     * Generic WebClient Builder
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
    
    /**
     * Metrics ve monitoring için connection pool bilgileri
     */
    @Bean("connectionPoolMetrics")
    public String connectionPoolInfo() {
        return "VoltDB Connection Pool: 100 max connections, 30s timeout, keep-alive enabled";
    }
}
