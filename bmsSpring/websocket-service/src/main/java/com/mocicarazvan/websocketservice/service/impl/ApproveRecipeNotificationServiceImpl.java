package com.mocicarazvan.websocketservice.service.impl;

import com.mocicarazvan.websocketservice.dtos.post.ApprovePostNotificationBody;
import com.mocicarazvan.websocketservice.dtos.post.ApprovePostNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.post.PostResponse;
import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationBody;
import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.mappers.ApprovePostNotificationMapper;
import com.mocicarazvan.websocketservice.mappers.ApproveRecipeNotificationMapper;
import com.mocicarazvan.websocketservice.models.*;
import com.mocicarazvan.websocketservice.repositories.ApprovePostNotificationRepository;
import com.mocicarazvan.websocketservice.repositories.ApproveRecipeNotificationRepository;
import com.mocicarazvan.websocketservice.repositories.PostRepository;
import com.mocicarazvan.websocketservice.repositories.RecipeRepository;
import com.mocicarazvan.websocketservice.service.ApprovePostNotificationService;
import com.mocicarazvan.websocketservice.service.ApproveRecipeNotificationService;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import com.mocicarazvan.websocketservice.service.generic.impl.ApproveNotificationServiceTemplateImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Service
public class ApproveRecipeNotificationServiceImpl
        extends ApproveNotificationServiceTemplateImpl
        <Recipe, RecipeResponse, ApproveRecipeNotification, ApproveRecipeNotificationBody, ApproveRecipeNotificationResponse,
                RecipeRepository, ApproveRecipeNotificationRepository,
                ApproveRecipeNotificationMapper>
        implements ApproveRecipeNotificationService {
    public ApproveRecipeNotificationServiceImpl(RecipeRepository referenceRepository, ConversationUserService conversationUserService, Executor asyncExecutor, ApproveRecipeNotificationRepository notificationTemplateRepository, ApproveRecipeNotificationMapper notificationTemplateMapper, SimpMessagingTemplate messagingTemplate) {
        super(referenceRepository, conversationUserService, "chat_recipe", "approveRecipeNotification", asyncExecutor, notificationTemplateRepository, notificationTemplateMapper, messagingTemplate);
    }

    @Override
    public Recipe createApprovedReference(ApproveRecipeNotificationBody body, Long appId, ConversationUser receiver) {
        return Recipe.builder()
                .approved(body.getType().equals(ApprovedNotificationType.APPROVED))
                .appId(appId)
                .receiver(receiver)
                .id(body.getReferenceId())
                .build();
    }

    @Override
    protected ApproveRecipeNotification createModelInstance(ConversationUser sender, ConversationUser receiver, ApprovedNotificationType type, Recipe reference, String content, String extraLink) {
        return ApproveRecipeNotification.builder()
                .sender(sender)
                .receiver(receiver)
                .type(type)
                .reference(reference)
                .content(content)
                .extraLink(extraLink)
                .build();
    }


}
