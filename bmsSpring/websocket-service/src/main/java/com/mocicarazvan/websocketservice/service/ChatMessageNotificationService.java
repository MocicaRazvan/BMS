package com.mocicarazvan.websocketservice.service;

import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.dtos.notifications.ChatMessageNotificationBody;
import com.mocicarazvan.websocketservice.dtos.notifications.ChatMessageNotificationResponse;
import com.mocicarazvan.websocketservice.enums.ChatMessageNotificationType;
import com.mocicarazvan.websocketservice.models.ChatRoom;
import com.mocicarazvan.websocketservice.service.generic.NotificationTemplateService;

public interface ChatMessageNotificationService extends
        NotificationTemplateService
                <ChatRoom, ChatRoomResponse, ChatMessageNotificationType, ChatMessageNotificationBody, ChatMessageNotificationResponse> {
}
