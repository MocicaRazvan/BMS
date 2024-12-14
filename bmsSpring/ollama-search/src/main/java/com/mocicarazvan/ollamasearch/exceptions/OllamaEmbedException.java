package com.mocicarazvan.ollamasearch.exceptions;

public class OllamaEmbedException extends RuntimeException {
    public OllamaEmbedException(String message) {
        super(message);
    }

    public OllamaEmbedException(String message, Throwable cause) {
        super(message, cause);
    }
}
