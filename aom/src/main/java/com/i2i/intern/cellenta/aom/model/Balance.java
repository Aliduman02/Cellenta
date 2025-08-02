package com.i2i.intern.cellenta.aom.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Balance {

    @JsonProperty("MSISDN")
    private String msisdn;

    @JsonProperty("PACKAGE_ID")
    private Long packageId;

    @JsonProperty("REMAINING_MINUTES")
    private int remainingMinutes;

    @JsonProperty("REMAINING_SMS")
    private int remainingSms;

    @JsonProperty("REMAINING_DATA")
    private int remainingData;

    @JsonProperty("START_DATE")
    private Timestamp startDate;

    @JsonProperty("END_DATE")
    private Timestamp endDate;

}
