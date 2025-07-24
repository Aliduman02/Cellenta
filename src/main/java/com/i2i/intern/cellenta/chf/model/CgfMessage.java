package com.i2i.intern.cellenta.chf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CgfMessage {
    
    @NotBlank(message = "Giver MSISDN cannot be blank")
    @JsonProperty("giver_msisdn")
    private String giverMsisdn;
    
    @JsonProperty("receiver_msisdn")
    private String receiverMsisdn;
    
    @JsonProperty("usage_date")
    private String usageDate;
    
    @NotNull(message = "Usage type cannot be null")
    @JsonProperty("usage_type")
    private String usageType;
    
    @NotNull(message = "Usage duration cannot be null")
    @JsonProperty("usage_duration")
    private Integer usageDuration;
    
    // Constructors
    public CgfMessage() {}
    
    public CgfMessage(String giverMsisdn, String receiverMsisdn, String usageDate, String usageType, Integer usageDuration) {
        this.giverMsisdn = giverMsisdn;
        this.receiverMsisdn = receiverMsisdn;
        this.usageDate = usageDate;
        this.usageType = usageType;
        this.usageDuration = usageDuration;
    }
    
    // Getters and Setters
    public String getGiverMsisdn() { return giverMsisdn; }
    public void setGiverMsisdn(String giverMsisdn) { this.giverMsisdn = giverMsisdn; }
    
    public String getReceiverMsisdn() { return receiverMsisdn; }
    public void setReceiverMsisdn(String receiverMsisdn) { this.receiverMsisdn = receiverMsisdn; }
    
    public String getUsageDate() { return usageDate; }
    public void setUsageDate(String usageDate) { this.usageDate = usageDate; }
    
    public String getUsageType() { return usageType; }
    public void setUsageType(String usageType) { this.usageType = usageType; }
    
    public Integer getUsageDuration() { return usageDuration; }
    public void setUsageDuration(Integer usageDuration) { this.usageDuration = usageDuration; }
    
    @Override
    public String toString() {
        return "CgfMessage{" +
                "giverMsisdn='" + giverMsisdn + '\'' +
                ", receiverMsisdn='" + receiverMsisdn + '\'' +
                ", usageDate='" + usageDate + '\'' +
                ", usageType='" + usageType + '\'' +
                ", usageDuration=" + usageDuration +
                '}';
    }
}
