package com.mocicarazvan.websocketservice.service.impl;

import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationBody;
import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.mappers.ApproveRecipeNotificationMapper;
import com.mocicarazvan.websocketservice.messaging.CustomConvertAndSendToUser;
import com.mocicarazvan.websocketservice.models.ApproveRecipeNotification;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.models.Recipe;
import com.mocicarazvan.websocketservice.repositories.ApproveRecipeNotificationRepository;
import com.mocicarazvan.websocketservice.repositories.RecipeRepository;
import com.mocicarazvan.websocketservice.service.ApproveRecipeNotificationService;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import com.mocicarazvan.websocketservice.service.generic.impl.ApproveNotificationServiceTemplateImpl;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ApproveRecipeNotificationServiceImpl
        extends ApproveNotificationServiceTemplateImpl
        <Recipe, RecipeResponse, ApproveRecipeNotification, ApproveRecipeNotificationBody, ApproveRecipeNotificationResponse,
                RecipeRepository, ApproveRecipeNotificationRepository,
                ApproveRecipeNotificationMapper>
        implements ApproveRecipeNotificationService {
    public ApproveRecipeNotificationServiceImpl(RecipeRepository referenceRepository, ConversationUserService conversationUserService, SimpleAsyncTaskExecutor asyncExecutor, ApproveRecipeNotificationRepository notificationTemplateRepository,
                                                ApproveRecipeNotificationMapper notificationTemplateMapper, SimpMessagingTemplate messagingTemplate,
                                                CustomConvertAndSendToUser customConvertAndSendToUser, TransactionTemplate transactionTemplate) {
        super(referenceRepository, conversationUserService, "chat_recipe", "approveRecipeNotification",
                asyncExecutor, notificationTemplateRepository, notificationTemplateMapper, messagingTemplate, customConvertAndSendToUser, transactionTemplate);
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
