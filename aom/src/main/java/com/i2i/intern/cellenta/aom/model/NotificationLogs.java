package com.i2i.intern.cellenta.aom.model;

import com.i2i.intern.cellenta.aom.model.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NotificationLogs {

    private Long id;
    private NotificationType notificationType;
    private LocalDateTime notificationTime;
    private Long customerId;

}
