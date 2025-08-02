package com.i2i.intern.cellenta.aom.model.Voltdb;

import lombok.Builder;

import java.util.List;

@Builder
public record VoltDBGetAllBalancesResponse(

        String status,
        List<VoltDBBalanceResponse> results

) {}