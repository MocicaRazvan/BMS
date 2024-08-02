package com.mocicarazvan.ingredientservice.validation;

import com.mocicarazvan.ingredientservice.validation.validators.FatCheckValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FatCheckValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FatCheck {
    String message() default "Saturated fat cannot be greater than fat";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}