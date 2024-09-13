package com.mocicarazvan.templatemodule.dtos.notifications;

import com.mocicarazvan.templatemodule.enums.ApprovedNotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@SuperBuilder
public class ApproveNotificationBody extends NotificationTemplateBody<ApprovedNotificationType> {

}
