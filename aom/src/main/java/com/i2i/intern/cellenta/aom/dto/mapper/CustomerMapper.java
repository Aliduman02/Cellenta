package com.i2i.intern.cellenta.aom.dto.mapper;

import com.i2i.intern.cellenta.aom.dto.response.CustomerResponse;
import com.i2i.intern.cellenta.aom.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toCustomerResponse(Customer customer) {
        return CustomerResponse.builder()
                .cust_id(customer.getCust_id())
                .email(customer.getEmail())
                .name(customer.getName())
                .surname(customer.getSurname())
                .msisdn(customer.getMsisdn())
                .sdate(customer.getSdate())
                .build();
    }

}
