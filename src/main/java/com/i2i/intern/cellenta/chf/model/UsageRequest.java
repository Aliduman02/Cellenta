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
        this.setMsisdn(msisdn);
        this.setCalledNumber(calledNumber);
        this.usageType = usageType;
        this.amount = amount;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public String getMsisdn() { return msisdn; }
    
    /**
     * MSISDN'i normalize eder - BAŞINDA 0 OLMAYACAK
     */
    public void setMsisdn(String msisdn) { 
        if (msisdn != null && !msisdn.trim().isEmpty()) {
            String cleanMsisdn = msisdn.replaceAll("[^0-9]", ""); // Sadece rakamlar
            
            if (cleanMsisdn.startsWith("90") && cleanMsisdn.length() == 12) {
                // 905551234567 -> 5551234567
                this.msisdn = cleanMsisdn.substring(2);
            } else if (cleanMsisdn.startsWith("0") && cleanMsisdn.length() == 11) {
                // 05551234567 -> 5551234567
                this.msisdn = cleanMsisdn.substring(1);
            } else if (cleanMsisdn.startsWith("5") && cleanMsisdn.length() == 10) {
                // 5551234567 -> 5551234567 (zaten doğru)
                this.msisdn = cleanMsisdn;
            } else {
                // Diğer durumlar için temizlenmiş hali
                this.msisdn = cleanMsisdn.startsWith("0") ? cleanMsisdn.substring(1) : cleanMsisdn;
            }
        } else {
            this.msisdn = msisdn;
        }
    }
    
    public String getCalledNumber() { return calledNumber; }
    
    /**
     * Called number'ı da normalize eder - BAŞINDA 0 OLMAYACAK
     */
    public void setCalledNumber(String calledNumber) { 
        if (calledNumber != null && !calledNumber.trim().isEmpty()) {
            String cleanNumber = calledNumber.replaceAll("[^0-9]", "");
            
            if (cleanNumber.startsWith("90") && cleanNumber.length() == 12) {
                // 905551234567 -> 5551234567
                this.calledNumber = cleanNumber.substring(2);
            } else if (cleanNumber.startsWith("0") && cleanNumber.length() == 11) {
                // 05551234567 -> 5551234567
                this.calledNumber = cleanNumber.substring(1);
            } else if (cleanNumber.startsWith("5") && cleanNumber.length() == 10) {
                // 5551234567 -> 5551234567 (zaten doğru)
                this.calledNumber = cleanNumber;
            } else {
                // Diğer durumlar için temizlenmiş hali
                this.calledNumber = cleanNumber.startsWith("0") ? cleanNumber.substring(1) : cleanNumber;
            }
        } else {
            this.calledNumber = calledNumber;
        }
    }
    
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
