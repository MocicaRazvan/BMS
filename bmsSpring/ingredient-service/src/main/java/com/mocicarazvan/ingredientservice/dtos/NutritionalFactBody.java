package com.mocicarazvan.ingredientservice.dtos;

import com.mocicarazvan.ingredientservice.enums.UnitType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
    @PositiveOrZero(message = "The fat should be positive.")
    private double fat;

    @NotNull(message = "The saturatedFat should not be null.")
    @PositiveOrZero(message = "The saturatedFat should be positive.")
    private double saturatedFat;

    @NotNull(message = "The carbohydrates should not be null.")
    @PositiveOrZero(message = "The carbohydrates should be positive.")
    private double carbohydrates;

    @NotNull(message = "The sugar should not be null.")
    @PositiveOrZero(message = "The sugar should be positive.")
    private double sugar;

    @NotNull(message = "The protein should not be null.")
    @PositiveOrZero(message = "The protein should be positive.")
    private double protein;

    @NotNull(message = "The salt should not be null.")
    @PositiveOrZero(message = "The salt should be positive.")
    private double salt;

    @NotNull(message = "The unit should not be null.")
    private UnitType unit;

    @AssertTrue(message = "The sum of fat, protein, carbohydrates, and salt must be greater than 0.")
    public boolean isMacronutrientSumValid() {
        double sum = fat + protein + carbohydrates + salt;
        return sum > 0;
    }

    @AssertTrue(message = "Saturated fat should be less than or equal to total fat.")
    public boolean isSaturatedFatValid() {
        return saturatedFat <= fat;
    }

    @AssertTrue(message = "Sugar should be less than or equal to total carbohydrates.")
    public boolean isSugarValid() {
        return sugar <= carbohydrates;
    }
}
