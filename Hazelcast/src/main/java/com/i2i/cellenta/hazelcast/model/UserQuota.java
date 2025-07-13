package com.i2i.cellenta.hazelcast.model;

import java.io.Serializable;

public class UserQuota implements Serializable {
    private int minutes;
    private int sms;
    private int data;

    /**
     * Represents the quota assigned to a user in the Online Charging System (OCS),
     * including voice minutes, SMS, and mobile data (MB or GB if you prefer).
     **/
    public UserQuota(int minutes, int sms, int data) {
        this.minutes = minutes;
        this.sms = sms;
        this.data = data;
    }

    // Getters and Setters
    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSms() {
        return sms;
    }

    public void setSms(int sms) {
        this.sms = sms;
    }

    public int getData() {
        return data;
    }
    public void setData(int data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "UsageQuota{" +
                "minutes=" + minutes +
                ", sms=" + sms +
                ", data=" + data +
                '}';
    }
}
