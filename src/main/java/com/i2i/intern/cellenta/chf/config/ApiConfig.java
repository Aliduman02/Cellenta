package com.i2i.intern.cellenta.chf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "chf.external-apis")
public class ApiConfig {
    
    private VoltDbConfig voltdb;
    
    public VoltDbConfig getVoltdb() { return voltdb; }
    public void setVoltdb(VoltDbConfig voltdb) { this.voltdb = voltdb; }
    
    public static class VoltDbConfig {
        private String baseUrl;
        private String balanceEndpoint;
        private String updateEndpoint;
        private Integer timeout;
        
        // Getters and Setters
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        
        public String getBalanceEndpoint() { return balanceEndpoint; }
        public void setBalanceEndpoint(String balanceEndpoint) { this.balanceEndpoint = balanceEndpoint; }
        
        public String getUpdateEndpoint() { return updateEndpoint; }
        public void setUpdateEndpoint(String updateEndpoint) { this.updateEndpoint = updateEndpoint; }
        
        public Integer getTimeout() { return timeout; }
        public void setTimeout(Integer timeout) { this.timeout = timeout; }
        
        public String getFullBalanceUrl() {
            return baseUrl + balanceEndpoint;
        }
        
        public String getFullUpdateUrl() {
            return baseUrl + updateEndpoint;
        }
    }
}
