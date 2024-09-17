package com.mocicarazvan.websocketservice.mappers;

import com.mocicarazvan.websocketservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.websocketservice.mappers.generic.ApproveModelMapper;
import com.mocicarazvan.websocketservice.models.Recipe;
import org.springframework.stereotype.Component;

@Component
public class RecipeMapper extends ApproveModelMapper<Recipe, RecipeResponse> {
    public RecipeMapper(ConversationUserMapper conversationUserMapper) {
        super(conversationUserMapper);
    }

    @Override
    public RecipeResponse fromModelToResponse(Recipe recipe) {
        return RecipeResponse.builder()
                .approved(recipe.isApproved())
                .receiver(conversationUserMapper.fromModelToResponse(recipe.getReceiver()))
                .appId(recipe.getAppId())
                .id(recipe.getId())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .build();
    }
}
