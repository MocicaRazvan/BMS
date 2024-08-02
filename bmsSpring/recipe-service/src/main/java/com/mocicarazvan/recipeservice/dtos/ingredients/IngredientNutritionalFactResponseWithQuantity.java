package com.mocicarazvan.recipeservice.dtos.ingredients;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class IngredientNutritionalFactResponseWithQuantity {
    private IngredientResponse ingredient;


    private NutritionalFactResponse nutritionalFact;
}
