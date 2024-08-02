package com.mocicarazvan.websocketservice.mappers;

import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.dtos.notifications.ChatMessageNotificationResponse;
import com.mocicarazvan.websocketservice.enums.ChatMessageNotificationType;
import com.mocicarazvan.websocketservice.mappers.generic.NotificationTemplateMapper;
import com.mocicarazvan.websocketservice.models.ChatMessageNotification;
import com.mocicarazvan.websocketservice.models.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageNotificationMapper
        extends NotificationTemplateMapper<ChatRoom, ChatRoomResponse, ChatMessageNotificationType,
        ChatMessageNotification, ChatMessageNotificationResponse> {

    private final ChatRoomMapper chatRoomMapper;
    private final ConversationUserMapper conversationUserMapper;

    @Override
    public ChatMessageNotificationResponse fromModelToResponse(ChatMessageNotification chatMessageNotification) {
        return ChatMessageNotificationResponse.builder()
                .id(chatMessageNotification.getId())
                .sender(conversationUserMapper.fromModelToResponse(chatMessageNotification.getSender()))
                .receiver(conversationUserMapper.fromModelToResponse(chatMessageNotification.getReceiver()))
                .type(chatMessageNotification.getType())
                .reference(chatRoomMapper.fromModelToResponse(chatMessageNotification.getReference()))
                .content(chatMessageNotification.getContent())
                .extraLink(chatMessageNotification.getExtraLink())
                .timestamp(chatMessageNotification.getTimestamp())
                .createdAt(chatMessageNotification.getCreatedAt())
                .updatedAt(chatMessageNotification.getUpdatedAt())
                .build();
    }


}
