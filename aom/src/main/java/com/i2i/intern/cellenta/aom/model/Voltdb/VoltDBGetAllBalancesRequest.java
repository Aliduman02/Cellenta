package com.i2i.intern.cellenta.aom.model.Voltdb;

import lombok.Builder;

@Builder
public record VoltDBGetAllBalancesRequest(
        String msisdn
) {}