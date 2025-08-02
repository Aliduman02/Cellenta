package com.i2i.intern.cellenta.aom.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record CreateBalanceRequest (

        @NotBlank(message = "MSISDN is required.")
        @Pattern(
                regexp = "^5\\d{9}$",
                message = "Telefon numarası 5 ile başlamalı ve toplam 10 haneli olmalıdır (örn: 5XXXXXXXXX)."
        )
        String msisdn,

        @NotNull(message = "Packet id is required.")
        @Min(value = 0, message = "Paket id 0'dan küçük olamaz.")
        Long packageId
){
}
