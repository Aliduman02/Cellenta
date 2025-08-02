package com.i2i.intern.cellenta.aom.controller;

import com.i2i.intern.cellenta.aom.model.ForgetPassword.SendCodeRequest;
import com.i2i.intern.cellenta.aom.repository.ForgetPasswordRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deneme-test-code")
@RequiredArgsConstructor
@Tag(name = "Test For Mail - Don't Use here", description = "")
public class TestSendCodeController {

    public final ForgetPasswordRepository forgetPasswordRepository;

    @Operation(summary = "Send Code endpoint")
    @PostMapping
    public Boolean sendCode(@RequestBody SendCodeRequest sendCodeRequest){
        return forgetPasswordRepository.sendMail(sendCodeRequest);
    }

    @GetMapping("/get-ip-address")
    public String getIpAddress(){
        return forgetPasswordRepository.mainUrl;
    }
}
