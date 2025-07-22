package com.i2i.intern.cellenta.aom.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.sql.Timestamp;

@Builder
@Schema(name = "Customer Response Model")
public record CustomerResponse(

        Long cust_id,
        String msisdn,
        String name,
        String surname,
        String email,
        Timestamp sdate

) {
}
