package com.i2i.intern.cellenta.aom.service;

import com.i2i.intern.cellenta.aom.dto.request.CheckVerifyCodeRequest;
import com.i2i.intern.cellenta.aom.dto.request.LoginRequest;
import com.i2i.intern.cellenta.aom.dto.request.RegisterRequest;
import com.i2i.intern.cellenta.aom.dto.response.ApiResponse;
import com.i2i.intern.cellenta.aom.dto.response.CustomerResponse;
import com.i2i.intern.cellenta.aom.model.enums.DeviceType;

public interface AuthService {

    CustomerResponse login(LoginRequest loginRequest, DeviceType deviceType, String ipAddress);
    CustomerResponse register(RegisterRequest registerRequest);

    ApiResponse forgetPassword(String email, DeviceType deviceType, String ipAddress);
    ApiResponse checkTheCode(CheckVerifyCodeRequest checkVerifyCodeRequest);

}
