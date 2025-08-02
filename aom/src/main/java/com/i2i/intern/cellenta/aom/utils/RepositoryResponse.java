package com.i2i.intern.cellenta.aom.utils;

import lombok.Builder;

@Builder
public record RepositoryResponse <T>(
        T result,
        int status
) {
}
