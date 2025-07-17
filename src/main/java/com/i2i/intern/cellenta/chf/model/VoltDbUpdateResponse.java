package com.i2i.intern.cellenta.chf.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VoltDbUpdateResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("status_code")
    private Integer statusCode;
    
    // Constructors
    public VoltDbUpdateResponse() {}
    
    public VoltDbUpdateResponse(String status, String message, Integer statusCode) {
        this.status = status;
        this.message = message;
        this.statusCode = statusCode;
    }
    
    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    
    @Override
    public String toString() {
        return "VoltDbUpdateResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", statusCode=" + statusCode +
                '}';
    }
}
