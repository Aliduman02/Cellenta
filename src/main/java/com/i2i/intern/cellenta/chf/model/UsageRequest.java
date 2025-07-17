package com.i2i.intern.cellenta.chf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class UsageRequest {
    
    @NotBlank(message = "MSISDN cannot be blank")
    @JsonProperty("msisdn")
    private String msisdn;
    
    @JsonProperty("called_number")
    private String calledNumber;
    
    @NotNull(message = "Usage type cannot be null")
    @JsonProperty("usage_type")
    private UsageType usageType;
    
    @Positive(message = "Amount must be positive")
    @JsonProperty("amount")
    private Integer amount;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    // Constructors
    public UsageRequest() {}
    
    public UsageRequest(String msisdn, String calledNumber, UsageType usageType, Integer amount, Long timestamp) {
        this.msisdn = msisdn;
        this.calledNumber = calledNumber;
        this.usageType = usageType;
        this.amount = amount;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public String getMsisdn() { return msisdn; }
    public void setMsisdn(String msisdn) { this.msisdn = msisdn; }
    
    public String getCalledNumber() { return calledNumber; }
    public void setCalledNumber(String calledNumber) { this.calledNumber = calledNumber; }
    
    public UsageType getUsageType() { return usageType; }
    public void setUsageType(UsageType usageType) { this.usageType = usageType; }
    
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return "UsageRequest{" +
                "msisdn='" + msisdn + '\'' +
                ", calledNumber='" + calledNumber + '\'' +
                ", usageType=" + usageType +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}
