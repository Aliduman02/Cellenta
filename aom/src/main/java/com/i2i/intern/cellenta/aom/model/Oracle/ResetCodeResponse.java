package com.i2i.intern.cellenta.aom.model.Oracle;

import lombok.Builder;

@Builder
public record ResetCodeResponse(
        int minutes,
        String code
) {
}
