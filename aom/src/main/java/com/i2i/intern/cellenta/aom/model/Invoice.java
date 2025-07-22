package com.i2i.intern.cellenta.aom.model;

import com.i2i.intern.cellenta.aom.model.enums.PaymentStatus;
import lombok.*;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {

    private Long id;
    private Long customerId;
    private Long packageId;

    private String startDate;
    private String endDate;
    private Double price;
    private PaymentStatus paymentStatus;
    private String isActive;
    private String daysLeft;

}
