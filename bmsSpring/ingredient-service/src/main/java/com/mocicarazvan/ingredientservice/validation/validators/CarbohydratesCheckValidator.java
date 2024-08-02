package com.mocicarazvan.ingredientservice.validation.validators;

import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactBody;
import com.mocicarazvan.ingredientservice.dtos.NutritionalFactBody;
import com.mocicarazvan.ingredientservice.validation.CarbohydratesCheck;
import com.mocicarazvan.ingredientservice.validation.FatCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CarbohydratesCheckValidator implements ConstraintValidator<CarbohydratesCheck, IngredientNutritionalFactBody> {

    @Override
    public boolean isValid(IngredientNutritionalFactBody value, ConstraintValidatorContext context) {

        if (value == null) {
            return true;
        }

        boolean isValid = value.getNutritionalFact().getSugar() <= value.getNutritionalFact().getCarbohydrates();

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Sugar cannot be greater than carbohydrates")
                    .addPropertyNode("sugar")
                    .addConstraintViolation();
        }

        return isValid;
    }
}