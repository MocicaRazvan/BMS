package com.mocicarazvan.recipeservice.exceptions;

import com.mocicarazvan.recipeservice.enums.DietType;
import lombok.Getter;

import java.util.List;

@Getter
public class InvalidTypeException extends RuntimeException {
    private final DietType type;
    private final List<DietType> givenTypes;

    public InvalidTypeException(DietType type, List<DietType> givenTypes) {
        super("Invalid type: " + type + ". Given types: " + givenTypes);
        this.type = type;
        this.givenTypes = givenTypes;
    }
}
