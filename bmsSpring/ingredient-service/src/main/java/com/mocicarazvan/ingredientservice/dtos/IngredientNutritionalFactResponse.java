package com.mocicarazvan.ingredientservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class IngredientNutritionalFactResponse {

    private IngredientResponse ingredient;


    private NutritionalFactResponse nutritionalFact;
}
