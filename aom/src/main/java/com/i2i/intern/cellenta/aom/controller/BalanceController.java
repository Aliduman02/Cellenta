package com.i2i.intern.cellenta.aom.controller;

import com.i2i.intern.cellenta.aom.dto.request.GetBalanceRequest;
import com.i2i.intern.cellenta.aom.dto.response.BalanceDetailResponse;
import com.i2i.intern.cellenta.aom.service.BalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/balance")
@RequiredArgsConstructor
@Tag(name = "Balance API", description = "Kullanıcı bakiye sorgulama işlemleri")
public class BalanceController {

    private final BalanceService balanceService;

    @Operation(summary = "Kullanıcı Msisdn ile kalan kullanımları döner")
    @PostMapping
    public ResponseEntity<BalanceDetailResponse> getBalance(
            @RequestBody GetBalanceRequest getBalanceRequest
    ) {
        BalanceDetailResponse balanceDetail = balanceService.getBalanceByCustomerId(getBalanceRequest.msisdn());
        return ResponseEntity.ok(balanceDetail);
    }

}