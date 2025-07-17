package com.i2i.intern.cellenta.chf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AbmfMessage {
    
    @NotBlank(message = "MSISDN cannot be blank")
    @JsonProperty("msisdn")
    private String msisdn;
    
    @NotNull(message = "New minutes cannot be null")
    @JsonProperty("new_minutes")
    private Integer newMinutes;
    
    @NotNull(message = "New SMS cannot be null")
    @JsonProperty("new_sms")
    private Integer newSms;
    
    @NotNull(message = "New data cannot be null")
    @JsonProperty("new_data")
    private Integer newData;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    // Constructors
    public AbmfMessage() {}
    
    public AbmfMessage(String msisdn, Integer newMinutes, Integer newSms, Integer newData, String timestamp) {
        this.msisdn = msisdn;
        this.newMinutes = newMinutes;
        this.newSms = newSms;
        this.newData = newData;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public String getMsisdn() { return msisdn; }
    public void setMsisdn(String msisdn) { this.msisdn = msisdn; }
    
    public Integer getNewMinutes() { return newMinutes; }
    public void setNewMinutes(Integer newMinutes) { this.newMinutes = newMinutes; }
    
    public Integer getNewSms() { return newSms; }
    public void setNewSms(Integer newSms) { this.newSms = newSms; }
    
    public Integer getNewData() { return newData; }
    public void setNewData(Integer newData) { this.newData = newData; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return "AbmfMessage{" +
                "msisdn='" + msisdn + '\'' +
                ", newMinutes=" + newMinutes +
                ", newSms=" + newSms +
                ", newData=" + newData +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
