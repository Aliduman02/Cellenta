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
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class VoltDbClient {
    
    private static final Logger logger = LoggerFactory.getLogger(VoltDbClient.class);
    
    private final WebClient webClient;
    private final ApiConfig apiConfig;
    
    @Autowired
    public VoltDbClient(WebClient webClient, ApiConfig apiConfig) {
        this.webClient = webClient;
        this.apiConfig = apiConfig;
    }
    
    /**
     * VoltDB'den balance bilgisini getir (POST metodu)
     * @param msisdn telefon numarası
     * @return VoltDbBalanceResponse
     */
    public Mono<VoltDbBalanceResponse> getBalance(String msisdn) {
        logger.info("Getting balance for MSISDN: {}", msisdn);
        
        // String'i Long'a çevir
        Long msisdnLong = Long.parseLong(msisdn);
        VoltDbBalanceRequest request = new VoltDbBalanceRequest(msisdnLong);
        
        return webClient
                .post()
                .uri(apiConfig.getVoltdb().getFullBalanceUrl())
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    logger.error("Client error while getting balance for MSISDN: {}, Status: {}", 
                               msisdn, response.statusCode());
                    return Mono.error(new RuntimeException("Client error: " + response.statusCode()));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    logger.error("Server error while getting balance for MSISDN: {}, Status: {}", 
                               msisdn, response.statusCode());
                    return Mono.error(new RuntimeException("Server error: " + response.statusCode()));
                })
                .bodyToMono(VoltDbBalanceResponse.class)
                .timeout(Duration.ofMillis(apiConfig.getVoltdb().getTimeout()))
                .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(30))
                        .doBeforeRetry(retrySignal -> 
                            logger.warn("Retrying balance request for MSISDN: {}, Attempt: {}", 
                                      msisdn, retrySignal.totalRetries() + 1)))
                .doOnSuccess(response -> {
                    if ("SUCCESS".equals(response.getStatus())) {
                        logger.info("Successfully retrieved balance for MSISDN: {}", msisdn);
                    } else {
                        logger.warn("Balance request returned non-success status for MSISDN: {}, Status: {}", 
                                   msisdn, response.getStatus());
                    }
                })
                .doOnError(error -> logger.error("Error getting balance for MSISDN: {}, Error: {}", 
                                                msisdn, error.getMessage()));
    }
    
    /**
     * VoltDB'ye balance güncelleme isteği gönder (POST metodu)
     * @param updateRequest güncellenecek balance bilgileri
     * @return VoltDbUpdateResponse
     */
    public Mono<VoltDbUpdateResponse> updateBalance(VoltDbUpdateRequest updateRequest) {
        logger.info("Updating balance for MSISDN: {}", updateRequest.getMsisdn());
        
        return webClient
                .post()
                .uri(apiConfig.getVoltdb().getFullUpdateUrl())
                .bodyValue(updateRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    logger.error("Client error while updating balance for MSISDN: {}, Status: {}", 
                               updateRequest.getMsisdn(), response.statusCode());
                    return Mono.error(new RuntimeException("Client error: " + response.statusCode()));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    logger.error("Server error while updating balance for MSISDN: {}, Status: {}", 
                               updateRequest.getMsisdn(), response.statusCode());
                    return Mono.error(new RuntimeException("Server error: " + response.statusCode()));
                })
                .bodyToMono(VoltDbUpdateResponse.class)
                .timeout(Duration.ofMillis(apiConfig.getVoltdb().getTimeout()))
                .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(30))
                        .doBeforeRetry(retrySignal -> 
                            logger.warn("Retrying update request for MSISDN: {}, Attempt: {}", 
                                      updateRequest.getMsisdn(), retrySignal.totalRetries() + 1)))
                .doOnSuccess(response -> {
                    if ("SUCCESS".equals(response.getStatus())) {
                        logger.info("Successfully updated balance for MSISDN: {}", updateRequest.getMsisdn());
                    } else {
                        logger.warn("Update request returned non-success status for MSISDN: {}, Status: {}", 
                                   updateRequest.getMsisdn(), response.getStatus());
                    }
                })
                .doOnError(error -> logger.error("Error updating balance for MSISDN: {}, Error: {}", 
                                                updateRequest.getMsisdn(), error.getMessage()));
    }
    
    /**
     * VoltDB bağlantısını test et
     * @return health check sonucu
     */
    public Mono<String> healthCheck() {
        logger.info("Performing VoltDB health check");
        
        return webClient
                .get()
                .uri(apiConfig.getVoltdb().getBaseUrl() + "/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(apiConfig.getVoltdb().getTimeout()))
                .doOnSuccess(response -> logger.info("VoltDB health check successful"))
                .doOnError(error -> logger.error("VoltDB health check failed: {}", error.getMessage()))
                .onErrorReturn("VoltDB service unavailable");
    }
}
