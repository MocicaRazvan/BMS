package com.mocicarazvan.ingredientservice.dtos;

import com.mocicarazvan.ingredientservice.enums.DietType;
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
