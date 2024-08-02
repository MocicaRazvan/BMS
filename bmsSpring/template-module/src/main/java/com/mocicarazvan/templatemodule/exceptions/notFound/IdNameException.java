package com.mocicarazvan.templatemodule.exceptions.notFound;

import lombok.Getter;

@Getter
public abstract class IdNameException extends NotFoundBase {
    private final String name;
    private final Long id;

    public IdNameException(String name, Long id, String message) {
        super(message);
        this.name = name;
        this.id = id;
    }
}
