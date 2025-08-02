package com.i2i.intern.cellenta.aom.dto.mapper;

import com.i2i.intern.cellenta.aom.dto.response.PackageResponse;
import com.i2i.intern.cellenta.aom.model.Paket;
import org.springframework.stereotype.Component;

@Component
public class PackageMapper {

    public PackageResponse toPackageResponse(Paket paket) {
        return PackageResponse.builder()
                .package_id(paket.getId())
                .period(paket.getPeriod())
                .packageName(paket.getPackageName())
                .amountSms(paket.getAmountSms())
                .amountData(paket.getAmountData())
                .amountMinutes(paket.getAmountMinutes())
                .price(paket.getPrice())
                .build();
    }

}
