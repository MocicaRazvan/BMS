package com.mocicarazvan.recipeservice.mappers;

import com.mocicarazvan.recipeservice.dtos.ingredients.IngredientQuantityDto;
import com.mocicarazvan.recipeservice.models.IngredientQuantity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
public class IngredientQuantityMapper {

    public IngredientQuantity fromDtoToModel(IngredientQuantityDto ingredientQuantityDto) {
        return IngredientQuantity.builder()
                .quantity(ingredientQuantityDto.getQuantity())
                .ingredientId(ingredientQuantityDto.getIngredientId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public IngredientQuantityDto fromModelToDto(IngredientQuantity ingredientQuantity) {
        return IngredientQuantityDto.builder()
                .quantity(ingredientQuantity.getQuantity())
                .ingredientId(ingredientQuantity.getIngredientId())
                .build();
    }
}
