package com.i2i.intern.cellenta.aom.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Paket {

    @JsonProperty("PACKAGE_ID")
    private Long id;

    @JsonProperty("PACKAGE_NAME")
    private String packageName;

    @JsonProperty("PRICE")
    private Double price;

    @JsonProperty("AMOUNT_MINUTES")
    private int amountMinutes;

    @JsonProperty("AMOUNT_DATA")
    private int amountData;

    @JsonProperty("AMOUNT_SMS")
    private int amountSms;

    @JsonProperty("PERIOD")
    private int period;

}
