package com.i2i.intern.cellenta.aom.controller;

import com.i2i.intern.cellenta.aom.dto.request.CheckVerifyCodeRequest;
import com.i2i.intern.cellenta.aom.dto.request.ForgetPasswordRequest;
import com.i2i.intern.cellenta.aom.dto.request.LoginRequest;
import com.i2i.intern.cellenta.aom.dto.request.RegisterRequest;
import com.i2i.intern.cellenta.aom.dto.response.ApiResponse;
import com.i2i.intern.cellenta.aom.dto.response.CustomerResponse;
import com.i2i.intern.cellenta.aom.model.enums.DeviceType;
import com.i2i.intern.cellenta.aom.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Login, register ve şifre işlemleri")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Kullanıcı girişi")
    @PostMapping("/login")
    public ResponseEntity<CustomerResponse> login(
            @RequestBody LoginRequest loginRequest,
            @RequestHeader(value = "Device-Type", required = false) DeviceType deviceType,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(authService.login(loginRequest, deviceType, ipAddress));
    }

    @Operation(summary = "Yeni kullanıcı kaydı")
    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Şifre sıfırlama isteği için mail gönderir")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> createCode(
            @RequestBody ForgetPasswordRequest forgetPasswordRequest,
            @RequestHeader(value = "Device-Type", required = false) DeviceType deviceType,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(authService.forgetPassword(forgetPasswordRequest.email(), deviceType, httpRequest.getRemoteAddr()));
    }

    @Operation(summary = "Şifre sıfırlama isteği için gönderilen kodu email ile kontrol eder")
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse> checkTheCode(
            @RequestBody CheckVerifyCodeRequest checkVerifyCodeRequest
    ) {
        return ResponseEntity.ok(authService.checkTheCode(checkVerifyCodeRequest));
    }

}