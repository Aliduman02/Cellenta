package com.i2i.intern.cellenta.aom.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ForgetPasswordRequest(

        @Email(message = "Geçerli bir e-posta adresi giriniz")
        @NotNull(message = "Please enter a valid e-mail")
        String email
) {
}
