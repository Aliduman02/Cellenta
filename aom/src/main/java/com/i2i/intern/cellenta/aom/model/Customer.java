package com.i2i.intern.cellenta.aom.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Customer {

    private Long cust_id;
    private String msisdn;
    private String name;
    private String surname;
    private String email;
    private String password;
    private Timestamp sdate;    // start date'den geliyor. kayıt olma zamanı

}
