package com.mocicarazvan.recipeservice.dtos.ingredients;

import com.mocicarazvan.recipeservice.enums.UnitType;
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
public class NutritionalFactResponse extends WithUserDto {


    private double fat;


    private double saturatedFat;


    private double carbohydrates;


    private double sugar;


    private double protein;


    private double salt;

    private UnitType unit;
}
