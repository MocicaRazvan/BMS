package com.mocicarazvan.ingredientservice.validation.validators;

import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactBody;
import com.mocicarazvan.ingredientservice.dtos.NutritionalFactBody;
import com.mocicarazvan.ingredientservice.validation.FatCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FatCheckValidator implements ConstraintValidator<FatCheck, IngredientNutritionalFactBody> {

    @Override
    public boolean isValid(IngredientNutritionalFactBody value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        boolean isValid = value.getNutritionalFact().getSaturatedFat() <= value.getNutritionalFact().getFat();

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Saturated fat cannot be greater than fat")
                    .addPropertyNode("saturatedFat")
                    .addConstraintViolation();
        }

        return isValid;
    }
}