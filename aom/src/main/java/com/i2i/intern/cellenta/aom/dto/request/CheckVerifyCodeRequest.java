package com.i2i.intern.cellenta.aom.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record CheckVerifyCodeRequest(

        @Email(message = "Please enter a valid email")
        String email,
        @Pattern(regexp = "\\d{6}", message = "Doğrulama kodu 6 haneli bir sayı olmalıdır")
        String code
) {
}
