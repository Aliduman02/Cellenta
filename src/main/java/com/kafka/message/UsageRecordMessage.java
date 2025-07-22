package com.kafka.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UsageRecordMessage implements Message {
    @JsonProperty("giver_msisdn")
    private String giverMsisdn;
    @JsonProperty("receiver_msisdn")
    private String receiverMsisdn;
    @JsonProperty("usage_date")
    private String usageDate;
    @JsonProperty("usage_type")
    private String usageType;
    @JsonProperty("usage_duration")
    private Integer usageDuration;

    public UsageRecordMessage(String giverMsisdn, String receiverMsisdn, String usageDate, String usageType, Integer usageDuration) {
        this.giverMsisdn = giverMsisdn;
        this.receiverMsisdn = receiverMsisdn;
        this.usageDate = usageDate;
        this.usageType = usageType;
        this.usageDuration = usageDuration;
    }

    public UsageRecordMessage() {}

    @Override
    public String getId() { return null; }
    @Override
    public void setId(String id) { /* intentionally left blank */ }
    @Override
    public String getTimestamp() { return null; }
    @Override
    public void setTimestamp(String timestamp) { /* intentionally left blank */ }

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
        return "UsageRecordMessage{" +
                "giverMsisdn='" + giverMsisdn + '\'' +
                ", receiverMsisdn='" + receiverMsisdn + '\'' +
                ", usageDate='" + usageDate + '\'' +
                ", usageType='" + usageType + '\'' +
                ", usageDuration=" + usageDuration +
                '}';
    }
} 
