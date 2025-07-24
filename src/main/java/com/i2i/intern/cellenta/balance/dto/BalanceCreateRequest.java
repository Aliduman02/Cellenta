package com.i2i.intern.cellenta.balance.dto;

import java.sql.Timestamp;

public class BalanceCreateRequest {
    public long msisdn;
    public int packageId;
    public int remainingMinutes;
    public int remainingSms;
    public int remainingData;
    public Timestamp startDate;
    public Timestamp endDate;
}
