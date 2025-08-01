package com.i2i.intern.cellenta.aom.service.impl;

import com.i2i.intern.cellenta.aom.dto.request.ChangePasswordRequest;
import com.i2i.intern.cellenta.aom.dto.request.GetInvoicesRequest;
import com.i2i.intern.cellenta.aom.dto.response.ApiResponse;
import com.i2i.intern.cellenta.aom.exception.UserNotFoundException;
import com.i2i.intern.cellenta.aom.model.Customer;
import com.i2i.intern.cellenta.aom.model.Invoice;
import com.i2i.intern.cellenta.aom.model.Voltdb.VoltDBCreateBalanceRequest;
import com.i2i.intern.cellenta.aom.repository.OracleRepository;
import com.i2i.intern.cellenta.aom.repository.VoltdbRepository;
import com.i2i.intern.cellenta.aom.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.i2i.cellenta.hazelcast.service.MsisdnService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final OracleRepository oracleRepository;
    private final VoltdbRepository voltdbRepository;

    @Override
    public ApiResponse addBalance(Long customerId, Long packageId) {
        oracleRepository.connect();
        boolean sonuc = false;
        try{
            sonuc =  oracleRepository.addNewBalanceToCustomer(customerId, packageId);

            if (sonuc) {
                Customer customer = oracleRepository.getCustomerById(customerId).
                        orElseThrow( () -> new UserNotFoundException("User not found with id " + customerId));

                // Hazelcast'e cachledik.
                MsisdnService.registerMsisdn(customer.getMsisdn());

                // balance değerini ise voltdb'ye yazdık
                /*Balance balance = oracleRepository.getBalanceByMsisdn(customer.getMsisdn())
                    .orElseThrow( () -> new BalanceNotFoundException("Balance not found with Msisdn: " + customerId));*/

                voltdbRepository.createNewBalance(
                        VoltDBCreateBalanceRequest.builder().msisdn(customer.getMsisdn()).packageId(packageId).build()
                );
            }
        }finally {
            oracleRepository.disconnect();
        }

        return ApiResponse.builder()
                .status(sonuc ? HttpStatus.CREATED.value() : HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .message(sonuc ? "Success" : "Failed")
                .error(sonuc ? "" : "Error")
                .path("/api/v1/customers/" + customerId + "/packages/" + packageId)
                .build();
    }

    @Override
    public ApiResponse changePassword(ChangePasswordRequest changePasswordRequest) {
        /*oracleRepository.connect();
        boolean checkCode = oracleRepository.checkTheCode(changePasswordRequest.email(), changePasswordRequest.verificationCode());
        if(!checkCode) throw new VerifyCodeNotFoudExcepiton("verification code didn't match or not found");*/
        oracleRepository.connect();
        boolean status = false;
        try{
            status = oracleRepository.changePassword(changePasswordRequest.email(), changePasswordRequest.password());
        }finally {
            oracleRepository.disconnect();
        }

        return ApiResponse.builder()
                .status( status ? 200 : 400)
                .timestamp(LocalDateTime.now())
                .message( status ? "Success" : "Fail")
                .error( status ? "" : "Failed")
                .path("/api/v1/customers/change-password: " + changePasswordRequest.email())
                .build();
    }

    @Override
    public List<Invoice> getInvoicesByMsisdn(GetInvoicesRequest getInvoicesRequest) {
        oracleRepository.connect();
        try{
            return oracleRepository.getInvoiceByMsisdn(getInvoicesRequest.msisdn());
        }finally {
            oracleRepository.disconnect();
        }
    }

}
