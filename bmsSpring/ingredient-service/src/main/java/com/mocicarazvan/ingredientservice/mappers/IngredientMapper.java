package com.mocicarazvan.ingredientservice.mappers;

import com.mocicarazvan.ingredientservice.dtos.IngredientBody;
import com.mocicarazvan.ingredientservice.dtos.IngredientResponse;
import com.mocicarazvan.ingredientservice.enums.DietType;
import com.mocicarazvan.ingredientservice.models.Ingredient;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import io.r2dbc.spi.Row;
import org.mapstruct.Mapper;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;


@Mapper(componentModel = "spring")
public abstract class IngredientMapper extends DtoMapper<Ingredient, IngredientBody, IngredientResponse> {
    @Override
    public IngredientResponse fromModelToResponse(Ingredient ingredient) {
        return IngredientResponse.builder()
                .userId(ingredient.getUserId())
                .id(ingredient.getId())
                .createdAt(ingredient.getCreatedAt())
                .updatedAt(ingredient.getUpdatedAt())
                .name(ingredient.getName())
                .type(ingredient.getType())
                .display(ingredient.isDisplay())
                .build();
    }

    @Override
    public Ingredient fromBodyToModel(IngredientBody ingredientBody) {
        return Ingredient.builder()
                .name(ingredientBody.getName())
                .type(ingredientBody.getType())
                .display(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public Mono<Ingredient> updateModelFromBody(IngredientBody ingredientBody, Ingredient ingredient) {
        ingredient.setName(ingredientBody.getName());
        ingredient.setType(ingredientBody.getType());
        ingredient.setDisplay(false);
        ingredient.setUpdatedAt(LocalDateTime.now());
        return Mono.just(ingredient);
    }

    public Ingredient fromRowToModel(Row row) {
        return Ingredient.builder()
                .id(row.get("id", Long.class))
                .createdAt(row.get("created_at", LocalDateTime.class))
                .updatedAt(row.get("updated_at", LocalDateTime.class))
                .userId(row.get("user_id", Long.class))
                .name(row.get("name", String.class))
                .type(DietType.valueOf(Objects.requireNonNull(row.get("type", String.class)).toUpperCase()))
                .display(Boolean.TRUE.equals(row.get("display", Boolean.class)))
                .build();
    }
}
