package com.i2i.intern.cellenta.aom.service.impl;

import com.i2i.intern.cellenta.aom.dto.mapper.CustomerMapper;
import com.i2i.intern.cellenta.aom.dto.request.CheckVerifyCodeRequest;
import com.i2i.intern.cellenta.aom.dto.request.LoginRequest;
import com.i2i.intern.cellenta.aom.dto.request.RegisterRequest;
import com.i2i.intern.cellenta.aom.exception.*;
import com.i2i.intern.cellenta.aom.model.ForgetPassword.SendCodeRequest;
import com.i2i.intern.cellenta.aom.dto.response.ApiResponse;
import com.i2i.intern.cellenta.aom.dto.response.CustomerResponse;
import com.i2i.intern.cellenta.aom.model.Customer;
import com.i2i.intern.cellenta.aom.model.Oracle.ResetCodeResponse;
import com.i2i.intern.cellenta.aom.model.enums.DeviceType;
import com.i2i.intern.cellenta.aom.repository.ForgetPasswordRepository;
import com.i2i.intern.cellenta.aom.repository.OracleRepository;
import com.i2i.intern.cellenta.aom.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Primary
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final OracleRepository oracleRepository;
    private final CustomerMapper customerMapper;
    private final ForgetPasswordRepository forgetPasswordRepository;

    @Override
    public CustomerResponse login(LoginRequest loginRequest, DeviceType deviceType, String ipAddress) {
        oracleRepository.connect();
        try {
            Customer customer = oracleRepository
                    .login(loginRequest.msisdn(), loginRequest.password(), deviceType.toString(), ipAddress)
                    .orElseThrow(() -> new UserNotFoundException("User not found with: " + loginRequest.msisdn()));

            return customerMapper.toCustomerResponse(customer);

        } finally {
            oracleRepository.disconnect();
        }
    }

    @Override
    public CustomerResponse register(RegisterRequest registerRequest) {
        oracleRepository.connect();
        try {
            Customer customer = oracleRepository.register(registerRequest.msisdn(), registerRequest.name(), registerRequest.surname(), registerRequest.email(), registerRequest.password())
                    .orElseThrow(() -> new UserRegistrationException("User not registered"));
            return customerMapper.toCustomerResponse(customer);
        }finally {
            oracleRepository.disconnect();
        }
    }

    @Override
    public ApiResponse forgetPassword(String email, DeviceType deviceType, String ipAddress) {
        oracleRepository.connect();
        ResetCodeResponse resetCodeResponse;
        try{
            resetCodeResponse = oracleRepository.getResetCode(email, deviceType.toString(), ipAddress).orElseThrow( () -> new VerifyCodeNotFoudExcepiton("Verify code oluşturulamadı: " + email));
        }finally {
            oracleRepository.disconnect();
        }
        boolean statusSendMail = forgetPasswordRepository.sendMail(SendCodeRequest.builder().code(resetCodeResponse.code()).minutes(resetCodeResponse.minutes()).email(email).build());
        return ApiResponse.builder()
                .path("/api/v1/auth/forget-password")
                .status(statusSendMail ? 201 : 500)
                .message(statusSendMail ? "Success" : "Fail")
                .error(statusSendMail ? "" : "Send mail failed")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public ApiResponse checkTheCode(CheckVerifyCodeRequest checkVerifyCodeRequest) {

        oracleRepository.connect();
        int status;
        String errorMessage = "";

        try {
            status = oracleRepository.checkTheCode(checkVerifyCodeRequest.email(), checkVerifyCodeRequest.code());
        } catch (GeneralOracleException e) {
            errorMessage = e.getMessage();
            throw e;
        } finally {
            oracleRepository.disconnect();
        }

        return ApiResponse.builder()
                .path("/api/v1/auth/verify-code")
                .status(status)
                .message(status == 200 ? "Success" : "Fail")
                .error(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }

}
