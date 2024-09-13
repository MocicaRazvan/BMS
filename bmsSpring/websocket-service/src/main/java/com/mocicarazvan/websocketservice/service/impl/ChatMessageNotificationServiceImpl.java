package com.mocicarazvan.websocketservice.service.impl;

import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.dtos.notifications.ChatMessageNotificationBody;
import com.mocicarazvan.websocketservice.dtos.notifications.ChatMessageNotificationResponse;
import com.mocicarazvan.websocketservice.enums.ChatMessageNotificationType;
import com.mocicarazvan.websocketservice.mappers.ChatMessageNotificationMapper;
import com.mocicarazvan.websocketservice.messaging.CustomConvertAndSendToUser;
import com.mocicarazvan.websocketservice.models.ChatMessageNotification;
import com.mocicarazvan.websocketservice.models.ChatRoom;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.repositories.ChatMessageNotificationRepository;
import com.mocicarazvan.websocketservice.repositories.ChatRoomRepository;
import com.mocicarazvan.websocketservice.service.ChatMessageNotificationService;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import com.mocicarazvan.websocketservice.service.generic.impl.NotificationTemplateServiceImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;


@Service
public class ChatMessageNotificationServiceImpl
        extends NotificationTemplateServiceImpl<
        ChatRoom, ChatRoomResponse, ChatMessageNotificationType, ChatMessageNotification,
        ChatMessageNotificationBody, ChatMessageNotificationResponse, ChatRoomRepository,
        ChatMessageNotificationRepository, ChatMessageNotificationMapper
        >
        implements ChatMessageNotificationService {
    public ChatMessageNotificationServiceImpl(ChatRoomRepository referenceRepository, ConversationUserService conversationUserService, Executor asyncExecutor,
                                              ChatMessageNotificationRepository notificationTemplateRepository, ChatMessageNotificationMapper notificationTemplateMapper,
                                              SimpMessagingTemplate messagingTemplate, CustomConvertAndSendToUser customConvertAndSendToUser) {
        super(referenceRepository, conversationUserService, "chatRoom", "chatMessageNotification", asyncExecutor, notificationTemplateRepository, notificationTemplateMapper, messagingTemplate, customConvertAndSendToUser);
    }

    @Override
    protected ChatMessageNotification createModelInstance(ConversationUser sender, ConversationUser receiver, ChatMessageNotificationType type, ChatRoom reference, String content, String extraLink) {
        return ChatMessageNotification.builder()
                .sender(sender)
                .receiver(receiver)
                .type(type)
                .reference(reference)
                .content(content)
                .extraLink(extraLink)
                .build();
    }
}
