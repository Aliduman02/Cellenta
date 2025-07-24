package com.i2i.intern.cellenta.chf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class VoltDbUpdateRequest {
    
    @NotNull(message = "MSISDN cannot be null")
    @JsonProperty("msisdn")
    private Long msisdn;
    
    @NotNull(message = "Remaining minutes cannot be null")
    @JsonProperty("remainingMinutes")
    private Integer remainingMinutes;
    
    @NotNull(message = "Remaining SMS cannot be null")
    @JsonProperty("remainingSms")
    private Integer remainingSms;
    
    @NotNull(message = "Remaining data cannot be null")
    @JsonProperty("remainingData")
    private Integer remainingData;
    
    // Constructors
    public VoltDbUpdateRequest() {}
    
    public VoltDbUpdateRequest(Long msisdn, Integer remainingMinutes, Integer remainingSms, Integer remainingData) {
        this.msisdn = msisdn;
        this.remainingMinutes = remainingMinutes;
        this.remainingSms = remainingSms;
        this.remainingData = remainingData;
    }
    
    // Getters and Setters
    public Long getMsisdn() { return msisdn; }
    public void setMsisdn(Long msisdn) { this.msisdn = msisdn; }
    
    public Integer getRemainingMinutes() { return remainingMinutes; }
    public void setRemainingMinutes(Integer remainingMinutes) { this.remainingMinutes = remainingMinutes; }
    
    public Integer getRemainingSms() { return remainingSms; }
    public void setRemainingSms(Integer remainingSms) { this.remainingSms = remainingSms; }
    
    public Integer getRemainingData() { return remainingData; }
    public void setRemainingData(Integer remainingData) { this.remainingData = remainingData; }
    
    @Override
    public String toString() {
        return "VoltDbUpdateRequest{" +
                "msisdn=" + msisdn +
                ", remainingMinutes=" + remainingMinutes +
                ", remainingSms=" + remainingSms +
                ", remainingData=" + remainingData +
                '}';
    }
}
