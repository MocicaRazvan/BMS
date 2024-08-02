package com.mocicarazvan.websocketservice.models;

import com.mocicarazvan.websocketservice.enums.ChatMessageNotificationType;
import com.mocicarazvan.websocketservice.models.generic.NotificationTemplate;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
//@AllArgsConstructor
@SuperBuilder
@Entity
public class ChatMessageNotification extends NotificationTemplate<ChatRoom, ChatMessageNotificationType> {
}
