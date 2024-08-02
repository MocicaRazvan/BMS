package com.mocicarazvan.websocketservice.dtos.notifications;

import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateBody;
import com.mocicarazvan.websocketservice.enums.ChatMessageNotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
//@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChatMessageNotificationBody extends NotificationTemplateBody<ChatMessageNotificationType> {
}
