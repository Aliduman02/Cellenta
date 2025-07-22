package com.i2i.intern.cellenta.aom.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ApiResponse (

        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path

){
}
