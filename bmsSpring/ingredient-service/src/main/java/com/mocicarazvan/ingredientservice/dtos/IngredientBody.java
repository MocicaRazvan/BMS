package com.mocicarazvan.ingredientservice.dtos;

import com.mocicarazvan.ingredientservice.enums.DietType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class IngredientBody {

    @NotBlank(message = "The name should not be empty.")
    private String name;

    @NotNull(message = "The type should not be null.")
    private DietType type;
}
