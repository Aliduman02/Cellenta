package com.i2i.intern.cellenta.chf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class ChfResponse {
    
    @NotBlank(message = "Status cannot be blank")
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("msisdn")
    private String msisdn;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    // Constructors
    public ChfResponse() {}
    
    public ChfResponse(String status, String message, String msisdn, String timestamp) {
        this.status = status;
        this.message = message;
        this.msisdn = msisdn;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getMsisdn() { return msisdn; }
    public void setMsisdn(String msisdn) { this.msisdn = msisdn; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return "ChfResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", msisdn='" + msisdn + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
