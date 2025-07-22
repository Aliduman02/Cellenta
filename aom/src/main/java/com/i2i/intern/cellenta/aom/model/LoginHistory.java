package com.i2i.intern.cellenta.aom.model;

import com.i2i.intern.cellenta.aom.model.enums.DeviceType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LoginHistory {

    private Long id;
    private Long customerId;
    private LocalDateTime loginTime;
    private DeviceType deviceType;
    private String ipAddress; // it can be null

}
