package com.mocicarazvan.ingredientservice.dtos;

import com.mocicarazvan.ingredientservice.validation.CarbohydratesCheck;
import com.mocicarazvan.ingredientservice.validation.FatCheck;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
@FatCheck
@CarbohydratesCheck
public class IngredientNutritionalFactBody {
    @NotNull(message = "The ingredient should not be null.")
    @Valid
    private IngredientBody ingredient;

    @NotNull(message = "The nutritionalFact should not be null.")
    @Valid
    private NutritionalFactBody nutritionalFact;
}
