package com.i2i.intern.cellenta.aom.model.ForgetPassword;

import lombok.Builder;

@Builder
public record SendCodeRequest(
        String email,
        String code,
        int minutes
) {
}
