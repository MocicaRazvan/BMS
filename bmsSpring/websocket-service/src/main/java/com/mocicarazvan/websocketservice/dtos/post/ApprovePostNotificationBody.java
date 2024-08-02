package com.mocicarazvan.websocketservice.dtos.post;

import com.mocicarazvan.websocketservice.dtos.generic.ApproveNotificationBody;
import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateBody;
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
public class ApprovePostNotificationBody extends ApproveNotificationBody {
}
