package com.mocicarazvan.websocketservice.mappers;

import com.mocicarazvan.websocketservice.dtos.post.ApprovePostNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.post.PostResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.mappers.generic.NotificationTemplateMapper;
import com.mocicarazvan.websocketservice.models.ApprovePostNotification;
import com.mocicarazvan.websocketservice.models.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApprovePostNotificationMapper extends NotificationTemplateMapper<Post, PostResponse, ApprovedNotificationType, ApprovePostNotification, ApprovePostNotificationResponse> {

    private final PostMapper postMapper;
    private final ConversationUserMapper conversationUserMapper;


    @Override
    public ApprovePostNotificationResponse fromModelToResponse(ApprovePostNotification approvePostNotification) {
        return ApprovePostNotificationResponse.builder()
                .id(approvePostNotification.getId())
                .sender(conversationUserMapper.fromModelToResponse(approvePostNotification.getSender()))
                .receiver(conversationUserMapper.fromModelToResponse(approvePostNotification.getReceiver()))
                .type(approvePostNotification.getType())
                .reference(postMapper.fromModelToResponse(approvePostNotification.getReference()))
                .content(approvePostNotification.getContent())
                .extraLink(approvePostNotification.getExtraLink())
                .timestamp(approvePostNotification.getTimestamp())
                .createdAt(approvePostNotification.getCreatedAt())
                .updatedAt(approvePostNotification.getUpdatedAt())
                .build();
    }
}
