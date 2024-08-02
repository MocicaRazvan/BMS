package com.mocicarazvan.ingredientservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class IngredientNutritionalFact {

    private Ingredient ingredient;
    private NutritionalFact nutritionalFact;
}
