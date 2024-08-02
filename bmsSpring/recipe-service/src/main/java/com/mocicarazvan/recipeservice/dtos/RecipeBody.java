package com.mocicarazvan.recipeservice.dtos;

import com.mocicarazvan.recipeservice.dtos.ingredients.IngredientQuantityDto;
import com.mocicarazvan.recipeservice.enums.DietType;
import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class RecipeBody extends TitleBodyDto {
    @NotNull(message = "The type should not be null.")
    private DietType type;

    @NotEmpty(message = "The ingredients should not be empty.")
    private List<IngredientQuantityDto> ingredients;
}
