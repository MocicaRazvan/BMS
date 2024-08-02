package com.mocicarazvan.recipeservice.dtos.ingredients;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class IngredientNutritionalFactResponseWithCount extends IngredientNutritionalFactResponse {
    private double count;

    public IngredientNutritionalFactResponseWithCount(IngredientNutritionalFactResponse ingredientNutritionalFactResponse, double count) {
        super(ingredientNutritionalFactResponse.getIngredient(), ingredientNutritionalFactResponse.getNutritionalFact());
        this.count = count;
    }
}
