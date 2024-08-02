package com.mocicarazvan.recipeservice.dtos.ingredients;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class IngredientQuantityDto {
    private double quantity;
    private Long ingredientId;
}
