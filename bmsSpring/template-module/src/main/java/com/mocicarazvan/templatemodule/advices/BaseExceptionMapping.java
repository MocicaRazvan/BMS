package com.mocicarazvan.templatemodule.advices;

import com.mocicarazvan.templatemodule.dtos.errors.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

public abstract class BaseExceptionMapping {
    protected BaseErrorResponse respWithMessage(HttpStatus status, Exception error, ServerWebExchange exchange) {
        return BaseErrorResponse.builder()
                .message(error.getMessage())
                .timestamp(Instant.now().toString())
                .error(status.getReasonPhrase())
                .status(status.value())
                .path(exchange.getRequest().getPath().value())
                .build();
    }

    protected Mono<ResponseEntity<BaseErrorResponse>> handleWithMessage(HttpStatus status, Exception exception, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(status).body(respWithMessage(status, exception, exchange)));
    }
}
