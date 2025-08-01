package com.i2i.intern.cellenta.aom.service;

import com.i2i.intern.cellenta.aom.dto.request.ChangePasswordRequest;
import com.i2i.intern.cellenta.aom.dto.request.GetInvoicesRequest;
import com.i2i.intern.cellenta.aom.dto.response.ApiResponse;
import com.i2i.intern.cellenta.aom.model.Invoice;

import java.util.List;

public interface CustomerService {

    ApiResponse addBalance(Long balanceId, Long customerId);
    ApiResponse changePassword(ChangePasswordRequest changePasswordRequest);
    List<Invoice> getInvoicesByMsisdn(GetInvoicesRequest getInvoicesRequest);
}
