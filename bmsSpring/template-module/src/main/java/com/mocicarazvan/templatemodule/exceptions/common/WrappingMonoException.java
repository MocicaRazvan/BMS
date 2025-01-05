package com.mocicarazvan.templatemodule.exceptions.common;


public class WrappingMonoException extends RuntimeException {
    public WrappingMonoException(String message, Throwable cause) {
        super(message, cause);
    }
}
