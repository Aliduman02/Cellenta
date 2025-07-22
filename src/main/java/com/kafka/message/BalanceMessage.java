package com.kafka.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BalanceMessage implements Message {
    private String timestamp;
    @JsonProperty("msisdn")
    private String msisdn;
    @JsonProperty("new_minutes")
    private Integer new_minutes;
    @JsonProperty("new_sms")
    private Integer new_sms;
    @JsonProperty("new_data")
    private Integer new_data;

    public BalanceMessage(String msisdn, Integer new_minutes, Integer new_sms, Integer new_data, String timestamp) {
        this.msisdn = msisdn;
        this.new_minutes = new_minutes;
        this.new_sms = new_sms;
        this.new_data = new_data;
        this.timestamp = timestamp;
    }

    public BalanceMessage() {}

    @Override
    public String getId() { return null; }
    @Override
    public void setId(String id) { /* intentionally left blank */ }
    @Override
    public String getTimestamp() { return timestamp; }
    @Override
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getMsisdn() { return msisdn; }
    public void setMsisdn(String msisdn) { this.msisdn = msisdn; }
    public Integer getNew_minutes() { return new_minutes; }
    public void setNew_minutes(Integer new_minutes) { this.new_minutes = new_minutes; }
    public Integer getNew_sms() { return new_sms; }
    public void setNew_sms(Integer new_sms) { this.new_sms = new_sms; }
    public Integer getNew_data() { return new_data; }
    public void setNew_data(Integer new_data) { this.new_data = new_data; }

    @Override
    public String toString() {
        return "BalanceMessage{" +
                "timestamp='" + timestamp + '\'' +
                ", msisdn='" + msisdn + '\'' +
                ", new_minutes=" + new_minutes +
                ", new_sms=" + new_sms +
                ", new_data=" + new_data +
                '}';
    }
} 