package com.i2i.intern.cellenta.chf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NotificationMessage {
    
    @NotBlank(message = "MSISDN cannot be blank")
    @JsonProperty("msisdn")
    private String msisdn;
    
    @NotNull(message = "Usage type cannot be null")
    @JsonProperty("usage_type")
    private String usageType;
    
    @NotNull(message = "Percentage cannot be null")
    @JsonProperty("percentage")
    private Integer percentage;
    
    @NotNull(message = "Remaining amount cannot be null")
    @JsonProperty("remaining_amount")
    private Integer remainingAmount;
    
    @JsonProperty("notification_message")
    private String notificationMessage;
    
    @JsonProperty("package_name")
    private String packageName;
    
    @JsonProperty("package_start_date")
    private String packageStartDate;
    
    @JsonProperty("package_end_date")
    private String packageEndDate;
    
    @JsonProperty("total_minutes")
    private Integer totalMinutes;
    
    @JsonProperty("total_sms")
    private Integer totalSms;
    
    @JsonProperty("total_data")
    private Integer totalData;
    
    @JsonProperty("remaining_minutes")
    private Integer remainingMinutes;
    
    @JsonProperty("remaining_sms")
    private Integer remainingSms;
    
    @JsonProperty("remaining_data")
    private Integer remainingData;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    // Constructors
    public NotificationMessage() {}
    
    public NotificationMessage(String msisdn, String usageType, Integer percentage, Integer remainingAmount, 
                             String notificationMessage, String packageName, String packageStartDate, String packageEndDate,
                             Integer totalMinutes, Integer totalSms, Integer totalData,
                             Integer remainingMinutes, Integer remainingSms, Integer remainingData, String timestamp) {
        this.msisdn = msisdn;
        this.usageType = usageType;
        this.percentage = percentage;
        this.remainingAmount = remainingAmount;
        this.notificationMessage = notificationMessage;
        this.packageName = packageName;
        this.packageStartDate = packageStartDate;
        this.packageEndDate = packageEndDate;
        this.totalMinutes = totalMinutes;
        this.totalSms = totalSms;
        this.totalData = totalData;
        this.remainingMinutes = remainingMinutes;
        this.remainingSms = remainingSms;
        this.remainingData = remainingData;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public String getMsisdn() { return msisdn; }
    public void setMsisdn(String msisdn) { this.msisdn = msisdn; }
    
    public String getUsageType() { return usageType; }
    public void setUsageType(String usageType) { this.usageType = usageType; }
    
    public Integer getPercentage() { return percentage; }
    public void setPercentage(Integer percentage) { this.percentage = percentage; }
    
    public Integer getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(Integer remainingAmount) { this.remainingAmount = remainingAmount; }
    
    public String getNotificationMessage() { return notificationMessage; }
    public void setNotificationMessage(String notificationMessage) { this.notificationMessage = notificationMessage; }
    
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    
    public String getPackageStartDate() { return packageStartDate; }
    public void setPackageStartDate(String packageStartDate) { this.packageStartDate = packageStartDate; }
    
    public String getPackageEndDate() { return packageEndDate; }
    public void setPackageEndDate(String packageEndDate) { this.packageEndDate = packageEndDate; }
    
    public Integer getTotalMinutes() { return totalMinutes; }
    public void setTotalMinutes(Integer totalMinutes) { this.totalMinutes = totalMinutes; }
    
    public Integer getTotalSms() { return totalSms; }
    public void setTotalSms(Integer totalSms) { this.totalSms = totalSms; }
    
    public Integer getTotalData() { return totalData; }
    public void setTotalData(Integer totalData) { this.totalData = totalData; }
    
    public Integer getRemainingMinutes() { return remainingMinutes; }
    public void setRemainingMinutes(Integer remainingMinutes) { this.remainingMinutes = remainingMinutes; }
    
    public Integer getRemainingSms() { return remainingSms; }
    public void setRemainingSms(Integer remainingSms) { this.remainingSms = remainingSms; }
    
    public Integer getRemainingData() { return remainingData; }
    public void setRemainingData(Integer remainingData) { this.remainingData = remainingData; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return "NotificationMessage{" +
                "msisdn='" + msisdn + '\'' +
                ", usageType='" + usageType + '\'' +
                ", percentage=" + percentage +
                ", remainingAmount=" + remainingAmount +
                ", notificationMessage='" + notificationMessage + '\'' +
                ", packageName='" + packageName + '\'' +
                ", packageStartDate='" + packageStartDate + '\'' +
                ", packageEndDate='" + packageEndDate + '\'' +
                ", totalMinutes=" + totalMinutes +
                ", totalSms=" + totalSms +
                ", totalData=" + totalData +
                ", remainingMinutes=" + remainingMinutes +
                ", remainingSms=" + remainingSms +
                ", remainingData=" + remainingData +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
