package com.mocicarazvan.ollamasearch.annotations;


import com.mocicarazvan.ollamasearch.exceptions.OllamaEmbedException;
import io.github.ollama4j.exceptions.OllamaBaseException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
        retryFor = {IOException.class,
                OllamaBaseException.class,
                InterruptedException.class,
                OllamaEmbedException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 200, multiplier = 2, maxDelay = 1000))
public @interface EmbedRetry {
}
