package com.i2i.intern.cellenta.aom.service.impl;

import com.i2i.intern.cellenta.aom.dto.response.BalanceDetailResponse;
import com.i2i.intern.cellenta.aom.exception.BalanceNotFoundException;
import com.i2i.intern.cellenta.aom.exception.PackageNotFoundException;
import com.i2i.intern.cellenta.aom.model.Balance;
import com.i2i.intern.cellenta.aom.model.Paket;
import com.i2i.intern.cellenta.aom.repository.OracleRepository;
import com.i2i.intern.cellenta.aom.repository.VoltdbRepository;
import com.i2i.intern.cellenta.aom.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final VoltdbRepository voltdbRepository;
    private final OracleRepository oracleRepository;

    @Override
    public BalanceDetailResponse getBalanceByCustomerId(String msisdn) {

        List<Balance> balances = voltdbRepository.getAllBalances(msisdn);
        Balance balance = balances.getFirst();

        if(balance == null)
            throw new BalanceNotFoundException("Balance not found with: " + msisdn);

        oracleRepository.connect();
        Paket paket = null;
        try {
            paket = oracleRepository.getPaketById(balance.getPackageId())
                    .orElseThrow(() -> new PackageNotFoundException("Paket not found with id: " + balance.getPackageId()));
        }finally {
            oracleRepository.disconnect();
        }

        return BalanceDetailResponse.builder()
                // Balance Information
                .remainingMinutes(balance.getRemainingMinutes())
                .remainingData(balance.getRemainingData())
                .remainingSms(balance.getRemainingSms())
                .sdate(balance.getStartDate())
                .edate(balance.getEndDate())

                // Package Information
                .packageName(paket.getPackageName())
                .price(paket.getPrice())
                .amountMinutes(paket.getAmountMinutes())
                .amountData(paket.getAmountData())
                .amountSms(paket.getAmountSms())
                .period(paket.getPeriod())
                .build();
    }

}
