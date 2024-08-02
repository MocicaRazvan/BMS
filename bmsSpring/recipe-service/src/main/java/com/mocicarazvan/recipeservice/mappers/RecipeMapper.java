package com.mocicarazvan.recipeservice.mappers;


import com.mocicarazvan.recipeservice.dtos.RecipeBody;
import com.mocicarazvan.recipeservice.dtos.RecipeResponse;
import com.mocicarazvan.recipeservice.enums.DietType;
import com.mocicarazvan.recipeservice.models.Recipe;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import io.r2dbc.spi.Row;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class RecipeMapper extends DtoMapper<Recipe, RecipeBody, RecipeResponse> {
    @Override
    public RecipeResponse fromModelToResponse(Recipe recipe) {
        return RecipeResponse.builder()
                .videos(recipe.getVideos())
                .type(recipe.getType())
                .approved(recipe.isApproved())
                .images(recipe.getImages())
                .body(recipe.getBody())
                .title(recipe.getTitle())
                .userLikes(recipe.getUserLikes())
                .userDislikes(recipe.getUserDislikes())
                .userId(recipe.getUserId())
                .id(recipe.getId())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .build();
    }

    @Override
    public Recipe fromBodyToModel(RecipeBody recipeBody) {
        return Recipe.builder()
                .type(recipeBody.getType())
                .title(recipeBody.getTitle())
                .body(recipeBody.getBody())
                .approved(false)
                .build();
    }

    @Override
    public Mono<Recipe> updateModelFromBody(RecipeBody recipeBody, Recipe recipe) {
        recipe.setType(recipeBody.getType());
        recipe.setTitle(recipeBody.getTitle());
        recipe.setBody(recipeBody.getBody());
        recipe.setApproved(false);
        recipe.setUpdatedAt(LocalDateTime.now());
        return Mono.just(recipe);
    }

    public Recipe fromRowToModel(Row row) {
        return Recipe.builder()
                .videos(EntitiesUtils.convertArrayToList(row.get("videos", String[].class)))
                .type(DietType.valueOf(Objects.requireNonNull(row.get("type", String.class)).toUpperCase()))
                .approved(Boolean.TRUE.equals(row.get("approved", Boolean.class)))
                .images(EntitiesUtils.convertArrayToList(row.get("images", String[].class)))
                .title(row.get("title", String.class))
                .body(row.get("body", String.class))
                .userLikes(EntitiesUtils.convertArrayToList(row.get("user_likes", Long[].class)))
                .userDislikes(EntitiesUtils.convertArrayToList(row.get("user_dislikes", Long[].class)))
                .userId(row.get("user_id", Long.class))
                .id(row.get("id", Long.class))
                .createdAt(row.get("created_at", LocalDateTime.class))
                .updatedAt(row.get("updated_at", LocalDateTime.class))
                .build();
    }

}
