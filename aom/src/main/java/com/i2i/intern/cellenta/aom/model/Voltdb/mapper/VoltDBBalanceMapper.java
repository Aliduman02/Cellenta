package com.i2i.intern.cellenta.aom.model.Voltdb.mapper;

import com.i2i.intern.cellenta.aom.model.Balance;
import com.i2i.intern.cellenta.aom.model.Voltdb.VoltDBBalanceResponse;

public class VoltDBBalanceMapper {

    public Balance toBalanceFromVoltDBBalanceResponse(VoltDBBalanceResponse voltDBBalanceResponse) {
        return Balance.builder()
                .startDate(voltDBBalanceResponse.startDate().toTimestamp())
                .endDate(voltDBBalanceResponse.endDate().toTimestamp())
                .packageId(voltDBBalanceResponse.packageId())
                .remainingSms(voltDBBalanceResponse.remainingSms())
                .remainingMinutes(voltDBBalanceResponse.remainingMinutes())
                .remainingData(voltDBBalanceResponse.remainingData())
                .msisdn(voltDBBalanceResponse.msisdn())
                .build();
    }

}
