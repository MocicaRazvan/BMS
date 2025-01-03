package com.mocicarazvan.archiveservice.validators.annotations;

import com.mocicarazvan.archiveservice.validators.QueueNameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = QueueNameValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidQueueName {
    String message() default "Invalid queue name";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
