package com.mocicarazvan.templatemodule.exceptions.action;

public class PrivateRouteException extends RuntimeException {
    public PrivateRouteException() {
        super("Not allowed!");
    }
}
