package com.i2i.intern.cellenta.aom.service;

import com.i2i.intern.cellenta.aom.dto.response.BalanceDetailResponse;

public interface BalanceService {

    BalanceDetailResponse getBalanceByCustomerId(String msisdn);

}
