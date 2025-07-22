package com.i2i.intern.cellenta.aom.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@Schema(name = "Login Request Model")
public record LoginRequest(

        @NotBlank(message = "MSISDN is required.")
        @Pattern(
                regexp = "^5\\d{9}$",
                message = "Telefon numarası 5 ile başlamalı ve toplam 10 haneli olmalıdır (örn: 5XXXXXXXXX)."
        )
        @Schema(example = "5552227799")
        String msisdn,

        @NotBlank(message = "Şifre boş olamaz.")
        @Schema(example = "123456Aa!")
        String password

) {
}
