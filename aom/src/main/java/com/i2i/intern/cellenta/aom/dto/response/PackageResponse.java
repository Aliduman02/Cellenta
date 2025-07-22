package com.i2i.intern.cellenta.aom.dto.response;

import lombok.Builder;

@Builder
public record PackageResponse(

        Long package_id,
        String packageName,
        double price,

        int amountMinutes,
        int amountData,
        int amountSms,

        int period // paket süresi (gün)
) {}