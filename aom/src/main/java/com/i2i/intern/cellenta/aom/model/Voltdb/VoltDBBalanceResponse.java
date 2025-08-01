package com.i2i.intern.cellenta.aom.model.Voltdb;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VoltDBBalanceResponse (

    @JsonProperty("MSISDN")
     String msisdn,

    @JsonProperty("PACKAGE_ID")
     Long packageId,

    @JsonProperty("REMAINING_MINUTES")
    int remainingMinutes,

    @JsonProperty("REMAINING_SMS")
    int remainingSms,

    @JsonProperty("REMAINING_DATA")
    int remainingData,

    @JsonProperty("START_DATE")
    MicroTime startDate,

    @JsonProperty("END_DATE")
    MicroTime endDate

){
}
