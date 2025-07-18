package com.cellenta.cgf.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsageRecord {
    private String giver_msisdn;
    private String receiver_msisdn;
    private String usage_date;
    private String usage_type;
    private int usage_duration;

    public String getGiver_msisdn() {
        return giver_msisdn;
    }

    public String getReceiver_msisdn() {
        return receiver_msisdn;
    }

    public int getUsage_duration() {
        return usage_duration;
    }

    public String getUsage_type() {
        return usage_type;
    }

    public String getUsage_date() {
        return usage_date;
    }

    public void setGiver_msisdn(String giver_msisdn) {
        this.giver_msisdn = giver_msisdn;
    }

    public void setReceiver_msisdn(String receiver_msisdn) {
        this.receiver_msisdn = receiver_msisdn;
    }

    public void setUsage_date(String usage_date) {
        this.usage_date = usage_date;
    }

    public void setUsage_type(String usage_type) {
        this.usage_type = usage_type;
    }

    public void setUsage_duration(int usage_duration) {
        this.usage_duration = usage_duration;
    }
}