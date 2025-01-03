package com.mocicarazvan.archiveservice.advices;


import com.mocicarazvan.archiveservice.dtos.errors.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestControllerAdvice
public class ArchiveAdvice {


    @ExceptionHandler(HandlerMethodValidationException.class)
    public Mono<ResponseEntity<BaseErrorResponse>> handleValidation(HandlerMethodValidationException e, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                BaseErrorResponse.builder()
                        .message(e.getMessage())
                        .timestamp(Instant.now().toString())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .path(exchange.getRequest().getPath().value())
                        .error("Validation Error: " + e.getAllValidationResults())
                        .build())
        );
    }
}
