package com.mocicarazvan.archiveservice.exceptions;


public class WrappingMonoException extends RuntimeException {
    public WrappingMonoException(String message, Throwable cause) {
        super(message, cause);
    }
}
