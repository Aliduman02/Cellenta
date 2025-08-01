package com.i2i.intern.cellenta.aom.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.sql.Timestamp;

@Builder
public record BalanceDetailResponse (

        int remainingMinutes,
        int remainingData,
        int remainingSms,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.S")
        Timestamp sdate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.S")
        Timestamp edate,

        String packageName,
        double price,
        int amountMinutes,
        int amountData,
        int amountSms,
        int period
){
}
