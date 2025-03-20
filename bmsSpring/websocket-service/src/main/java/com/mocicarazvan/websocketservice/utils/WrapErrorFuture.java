package com.mocicarazvan.websocketservice.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;

@Slf4j
public class WrapErrorFuture {

    public static <T> T wrapCallable(Callable<T> callable
    ) {
        try {
            return callable.call();
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            } else {
                throw new CompletionException(e);
            }
        }
    }
}