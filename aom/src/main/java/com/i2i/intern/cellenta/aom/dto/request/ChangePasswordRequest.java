package com.i2i.intern.cellenta.aom.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record ChangePasswordRequest(

        @Email(message = "Lütfen geçerli bir email giriniz.")
                @Schema(example = "user@example.com")
        String email,

        @NotBlank(message = "Şifre boş olamaz.")
                @Schema(example = "sifre123Aa!")
        String password
        /*,
        @Pattern(regexp = "\\d{6}", message = "Doğrulama kodu 6 haneli bir sayı olmalıdır")
        String verificationCode*/
) {
}
