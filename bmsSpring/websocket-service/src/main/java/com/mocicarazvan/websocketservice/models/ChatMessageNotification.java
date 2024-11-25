package com.mocicarazvan.websocketservice.models;

import com.mocicarazvan.websocketservice.enums.ChatMessageNotificationType;
import com.mocicarazvan.websocketservice.models.generic.NotificationTemplate;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
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
@Table(
        indexes = {
                @Index(name = "idx_sender_chat_message_notification_id", columnList = "sender_id"),
                @Index(name = "idx_receiver_chat_message_notification_id", columnList = "receiver_id"),
                @Index(name = "idx_reference_chat_message_notification_id", columnList = "reference_id"),
        }
)
public class ChatMessageNotification extends NotificationTemplate<ChatRoom, ChatMessageNotificationType> {
}
