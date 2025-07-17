package com.i2i.intern.cellenta.chf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class VoltDbBalanceResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("results")
    private List<BalanceResult> results;
    
    // Constructors
    public VoltDbBalanceResponse() {}
    
    public VoltDbBalanceResponse(String status, List<BalanceResult> results) {
        this.status = status;
        this.results = results;
    }
    
    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<BalanceResult> getResults() { return results; }
    public void setResults(List<BalanceResult> results) { this.results = results; }
    
    @Override
    public String toString() {
        return "VoltDbBalanceResponse{" +
                "status='" + status + '\'' +
                ", results=" + results +
                '}';
    }
    
    // Inner class for results
    public static class BalanceResult {
        
        @JsonProperty("MSISDN")
        private Long msisdn;
        
        @JsonProperty("REMAINING_MINUTES")
        private Integer remainingMinutes;
        
        @JsonProperty("REMAINING_SMS")
        private Integer remainingSms;
        
        @JsonProperty("REMAINING_DATA")
        private Integer remainingData;
        
        @JsonProperty("START_DATE")
        private String startDate;
        
        @JsonProperty("END_DATE")
        private String endDate;
        
        @JsonProperty("PACKAGE_NAME")
        private String packageName;
        
        @JsonProperty("PRICE")
        private Integer price;
        
        @JsonProperty("AMOUNT_MINUTES")
        private Integer amountMinutes;
        
        @JsonProperty("AMOUNT_SMS")
        private Integer amountSms;
        
        @JsonProperty("AMOUNT_DATA")
        private Integer amountData;
        
        @JsonProperty("PERIOD")
        private Integer period;
        
        // Constructors
        public BalanceResult() {}
        
        // Getters and Setters
        public Long getMsisdn() { return msisdn; }
        public void setMsisdn(Long msisdn) { this.msisdn = msisdn; }
        
        public Integer getRemainingMinutes() { return remainingMinutes; }
        public void setRemainingMinutes(Integer remainingMinutes) { this.remainingMinutes = remainingMinutes; }
        
        public Integer getRemainingSms() { return remainingSms; }
        public void setRemainingSms(Integer remainingSms) { this.remainingSms = remainingSms; }
        
        public Integer getRemainingData() { return remainingData; }
        public void setRemainingData(Integer remainingData) { this.remainingData = remainingData; }
        
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        
        public Integer getPrice() { return price; }
        public void setPrice(Integer price) { this.price = price; }
        
        public Integer getAmountMinutes() { return amountMinutes; }
        public void setAmountMinutes(Integer amountMinutes) { this.amountMinutes = amountMinutes; }
        
        public Integer getAmountSms() { return amountSms; }
        public void setAmountSms(Integer amountSms) { this.amountSms = amountSms; }
        
        public Integer getAmountData() { return amountData; }
        public void setAmountData(Integer amountData) { this.amountData = amountData; }
        
        public Integer getPeriod() { return period; }
        public void setPeriod(Integer period) { this.period = period; }
        
        @Override
        public String toString() {
            return "BalanceResult{" +
                    "msisdn=" + msisdn +
                    ", remainingMinutes=" + remainingMinutes +
                    ", remainingSms=" + remainingSms +
                    ", remainingData=" + remainingData +
                    ", startDate='" + startDate + '\'' +
                    ", endDate='" + endDate + '\'' +
                    ", packageName='" + packageName + '\'' +
                    ", price=" + price +
                    ", amountMinutes=" + amountMinutes +
                    ", amountSms=" + amountSms +
                    ", amountData=" + amountData +
                    ", period=" + period +
                    '}';
        }
    }
}
