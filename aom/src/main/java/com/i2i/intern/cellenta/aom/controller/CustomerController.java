package com.i2i.intern.cellenta.aom.controller;

import com.i2i.intern.cellenta.aom.dto.request.ChangePasswordRequest;
import com.i2i.intern.cellenta.aom.dto.request.GetInvoicesRequest;
import com.i2i.intern.cellenta.aom.dto.response.ApiResponse;
import com.i2i.intern.cellenta.aom.model.Invoice;
import com.i2i.intern.cellenta.aom.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer API", description = "Müşteri profili işlemleri")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "User'a istediği tarifeyi ekleme metodudur. Eklendiğinde Success şeklinde dönüş yapılır")
    @PostMapping("/{customerId}/package/{packageId}")
    public ResponseEntity<ApiResponse> addBalance(
            @PathVariable Long customerId,
            @PathVariable Long packageId
    ) {
        return ResponseEntity.ok(customerService.addBalance(customerId, packageId));
    }

    @Operation(summary = "Verify code başarılı olduğunda şifreyi değiştirmemize olan sağlayan endpoint")
    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @RequestBody ChangePasswordRequest changePasswordRequest
    ) {
        return ResponseEntity.ok(customerService.changePassword(changePasswordRequest));
    }

    @Operation(summary = "Msisdn üzerinden kullanıcı faturalarını bizlere döndurur.")
    @PostMapping("/invoices")
    public ResponseEntity<List<Invoice>> getInvoicesByMsisdn(
            @RequestBody GetInvoicesRequest getInvoicesRequest
    ) {
        return ResponseEntity.ok(customerService.getInvoicesByMsisdn(getInvoicesRequest));
    }

    /*@Operation(summary = "ID ile müşteri bilgisi getir")
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerById(customerId));
    }


    @Operation(summary = "Tüm müşterileri getir")
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        //List<CustomerResponse> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(null);
    }*/

    /*@Operation(summary = "Müşteri bilgisini güncelle")
    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable String customerId,
            @RequestBody CustomerUpdateRequest customerUpdateRequest) {
        //CustomerResponse updatedCustomer = customerService.updateCustomer(customerId, customerUpdateRequest);
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Müşteri sil")
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String customerId) {
        //customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }*/
}