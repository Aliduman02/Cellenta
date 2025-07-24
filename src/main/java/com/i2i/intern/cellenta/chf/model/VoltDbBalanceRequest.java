package com.i2i.intern.cellenta.chf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class VoltDbBalanceRequest {
    
    @NotNull(message = "MSISDN cannot be null")
    @JsonProperty("msisdn")
    private Long msisdn;
    
    // Constructors
    public VoltDbBalanceRequest() {}
    
    public VoltDbBalanceRequest(Long msisdn) {
        this.msisdn = msisdn;
    }
    
    // Getters and Setters
    public Long getMsisdn() { return msisdn; }
    public void setMsisdn(Long msisdn) { this.msisdn = msisdn; }
    
    @Override
    public String toString() {
        return "VoltDbBalanceRequest{" +
                "msisdn=" + msisdn +
                '}';
    }
}
