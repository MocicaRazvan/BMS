package com.mocicarazvan.ingredientservice.validation;

import com.mocicarazvan.ingredientservice.validation.validators.CarbohydratesCheckValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CarbohydratesCheckValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CarbohydratesCheck {
    String message() default "Sugar cannot be greater than carbohydrates";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}