package com.i2i.intern.cellenta.aom.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record RegisterRequest(

        @NotBlank(message = "MSISDN is required.")
        @Pattern(
                regexp = "^5\\d{9}$",
                message = "Telefon numarası 5 ile başlamalı ve toplam 10 haneli olmalıdır (örn: 5XXXXXXXXX)."
        )
        @Schema(example = "5551234567")
        String msisdn,

        @NotBlank(message = "Password is required.")

        @Schema(example = "sifre123A!")
        String password,

        String name,

        String surname,

        @Email(message = "Please enter a valid e-mail")
        @NotBlank(message = "E-Mail is required")
        @Schema(example = "user@example.com")
        String email

) {
}
