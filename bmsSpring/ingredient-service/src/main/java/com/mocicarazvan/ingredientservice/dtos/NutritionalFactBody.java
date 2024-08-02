package com.mocicarazvan.ingredientservice.dtos;

import com.mocicarazvan.ingredientservice.enums.UnitType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class NutritionalFactBody {

    @NotNull(message = "The fat should not be null.")
    @Positive(message = "The fat should be positive.")
    private double fat;

    @NotNull(message = "The saturatedFat should not be null.")
    @Positive(message = "The saturatedFat should be positive.")
    private double saturatedFat;

    @NotNull(message = "The carbohydrates should not be null.")
    @Positive(message = "The carbohydrates should be positive.")
    private double carbohydrates;

    @NotNull(message = "The sugar should not be null.")
    @Positive(message = "The sugar should be positive.")
    private double sugar;

    @NotNull(message = "The protein should not be null.")
    @Positive(message = "The protein should be positive.")
    private double protein;

    @NotNull(message = "The salt should not be null.")
    @Positive(message = "The salt should be positive.")
    private double salt;

    @NotNull(message = "The unit should not be null.")
    private UnitType unit;
}
