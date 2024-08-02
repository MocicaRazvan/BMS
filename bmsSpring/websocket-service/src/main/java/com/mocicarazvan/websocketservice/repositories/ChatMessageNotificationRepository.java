package com.mocicarazvan.websocketservice.repositories;

import com.mocicarazvan.websocketservice.enums.ChatMessageNotificationType;
import com.mocicarazvan.websocketservice.models.ChatMessageNotification;
import com.mocicarazvan.websocketservice.models.ChatRoom;
import com.mocicarazvan.websocketservice.repositories.generic.NotificationTemplateRepository;


public interface ChatMessageNotificationRepository extends
        NotificationTemplateRepository<ChatRoom, ChatMessageNotificationType, ChatMessageNotification> {
}
