package com.mocicarazvan.websocketservice.mappers;

import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.mappers.generic.NotificationTemplateMapper;
import com.mocicarazvan.websocketservice.models.ApproveRecipeNotification;
import com.mocicarazvan.websocketservice.models.Recipe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApproveRecipeNotificationMapper extends NotificationTemplateMapper<Recipe, RecipeResponse, ApprovedNotificationType, ApproveRecipeNotification, ApproveRecipeNotificationResponse> {

    private final RecipeMapper recipeMapper;
    private final ConversationUserMapper conversationUserMapper;


    @Override
    public ApproveRecipeNotificationResponse fromModelToResponse(ApproveRecipeNotification approveRecipeNotification) {
        return ApproveRecipeNotificationResponse.builder()
                .id(approveRecipeNotification.getId())
                .sender(conversationUserMapper.fromModelToResponse(approveRecipeNotification.getSender()))
                .receiver(conversationUserMapper.fromModelToResponse(approveRecipeNotification.getReceiver()))
                .type(approveRecipeNotification.getType())
                .reference(recipeMapper.fromModelToResponse(approveRecipeNotification.getReference()))
                .content(approveRecipeNotification.getContent())
                .extraLink(approveRecipeNotification.getExtraLink())
                .timestamp(approveRecipeNotification.getTimestamp())
                .createdAt(approveRecipeNotification.getCreatedAt())
                .updatedAt(approveRecipeNotification.getUpdatedAt())
                .build();
    }
}
