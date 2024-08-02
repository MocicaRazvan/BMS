package com.mocicarazvan.recipeservice.dtos.ingredients;

import com.mocicarazvan.recipeservice.enums.DietType;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
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
public class IngredientResponse extends WithUserDto {
    private String name;
    private DietType type;
    private boolean display;
}
