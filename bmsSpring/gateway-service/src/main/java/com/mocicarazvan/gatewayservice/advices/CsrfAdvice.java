package com.mocicarazvan.gatewayservice.advices;

import com.mocicarazvan.gatewayservice.dtos.errors.BaseErrorResponse;
import com.mocicarazvan.gatewayservice.dtos.errors.ValidationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;

@RestControllerAdvice
public class CsrfAdvice {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ValidationResponse>> handleValidation(WebExchangeBindException e, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ValidationResponse().withBase(respWithMessage(HttpStatus.BAD_REQUEST, e, exchange), e.getBindingResult().getFieldErrors().stream().collect(
                        HashMap::new,
                        (m, v) -> m.put(v.getField(), v.getDefaultMessage()),
                        HashMap::putAll
                ))
        ));
    }

    private BaseErrorResponse respWithMessage(HttpStatus status, Exception error, ServerWebExchange exchange) {
        return BaseErrorResponse.builder()
                .message(error.getMessage())
                .timestamp(Instant.now().toString())
                .error(status.getReasonPhrase())
                .status(status.value())
                .path(exchange.getRequest().getPath().value())
                .build();
    }


}
