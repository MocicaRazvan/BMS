package com.mocicarazvan.websocketservice.service.impl;

import com.mocicarazvan.websocketservice.dtos.post.ApprovePostNotificationBody;
import com.mocicarazvan.websocketservice.dtos.post.ApprovePostNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.post.PostResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.mappers.ApprovePostNotificationMapper;
import com.mocicarazvan.websocketservice.messaging.CustomConvertAndSendToUser;
import com.mocicarazvan.websocketservice.models.ApprovePostNotification;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.models.Post;
import com.mocicarazvan.websocketservice.repositories.ApprovePostNotificationRepository;
import com.mocicarazvan.websocketservice.repositories.PostRepository;
import com.mocicarazvan.websocketservice.service.ApprovePostNotificationService;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import com.mocicarazvan.websocketservice.service.generic.impl.ApproveNotificationServiceTemplateImpl;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ApprovePostNotificationServiceImpl
        extends ApproveNotificationServiceTemplateImpl
        <Post, PostResponse, ApprovePostNotification, ApprovePostNotificationBody, ApprovePostNotificationResponse, PostRepository, ApprovePostNotificationRepository, ApprovePostNotificationMapper>
        implements ApprovePostNotificationService {
    public ApprovePostNotificationServiceImpl(PostRepository referenceRepository, ConversationUserService conversationUserService, SimpleAsyncTaskExecutor asyncExecutor, ApprovePostNotificationRepository notificationTemplateRepository, ApprovePostNotificationMapper notificationTemplateMapper, SimpMessagingTemplate messagingTemplate, CustomConvertAndSendToUser customConvertAndSendToUser) {
        super(referenceRepository, conversationUserService, "chat_post", "approvePostNotification", asyncExecutor, notificationTemplateRepository, notificationTemplateMapper, messagingTemplate, customConvertAndSendToUser);
    }

    @Override
    public Post createApprovedReference(ApprovePostNotificationBody body, Long appId, ConversationUser receiver) {
        return Post.builder()
                .approved(body.getType().equals(ApprovedNotificationType.APPROVED))
                .appId(appId)
                .receiver(receiver)
                .id(body.getReferenceId())
                .build();
    }


    @Override
    protected ApprovePostNotification createModelInstance(ConversationUser sender, ConversationUser receiver, ApprovedNotificationType type, Post reference, String content, String extraLink) {
        return ApprovePostNotification.builder()
                .sender(sender)
                .receiver(receiver)
                .type(type)
                .reference(reference)
                .content(content)
                .extraLink(extraLink)
                .build();
    }


}
