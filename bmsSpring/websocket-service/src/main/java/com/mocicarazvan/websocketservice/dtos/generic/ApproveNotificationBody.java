package com.mocicarazvan.websocketservice.dtos.generic;

import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
//@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class ApproveNotificationBody extends NotificationTemplateBody<ApprovedNotificationType> {

}
