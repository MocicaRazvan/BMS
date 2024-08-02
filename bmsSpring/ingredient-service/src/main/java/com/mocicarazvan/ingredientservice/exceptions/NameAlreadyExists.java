package com.mocicarazvan.ingredientservice.exceptions;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NameAlreadyExists extends RuntimeException {

    private String message;
    private String name;

    public NameAlreadyExists(String message, String name) {
        this.message = message;
        this.name = name;
    }
}
