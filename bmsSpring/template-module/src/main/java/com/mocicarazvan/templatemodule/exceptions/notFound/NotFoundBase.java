package com.mocicarazvan.templatemodule.exceptions.notFound;

public abstract class NotFoundBase extends RuntimeException {
    public NotFoundBase(String message) {
        super(message);
    }
}
