package com.mocicarazvan.websocketservice.service;

import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationBody;
import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.websocketservice.models.Recipe;
import com.mocicarazvan.websocketservice.service.generic.ApproveNotificationServiceTemplate;

public interface ApproveRecipeNotificationService extends ApproveNotificationServiceTemplate<Recipe, RecipeResponse,
        ApproveRecipeNotificationBody, ApproveRecipeNotificationResponse
        > {
}
