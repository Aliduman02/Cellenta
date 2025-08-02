package com.i2i.intern.cellenta.aom.model;


import com.i2i.intern.cellenta.aom.model.enums.UsageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PersonalUsage {

    private Long id;
    private String giverMsisdn;    // arayan kişinin tel_no
    private String receiverMsisdn; // aranan kişinin tel_no

    private LocalDateTime usageDate;  // ne zaman aradığı bilgisi
    private UsageType usageType;
    private int usageDuration; // kullanım süresi aramalar için

}
