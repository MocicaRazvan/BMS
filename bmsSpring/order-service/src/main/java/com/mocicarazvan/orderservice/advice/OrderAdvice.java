package com.mocicarazvan.orderservice.advice;

import com.mocicarazvan.orderservice.exceptions.CustomerStripeException;
import com.mocicarazvan.templatemodule.advices.BaseAdvice;
import com.mocicarazvan.templatemodule.dtos.errors.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class OrderAdvice extends BaseAdvice {

    @ExceptionHandler(CustomerStripeException.class)
    public Mono<ResponseEntity<BaseErrorResponse>> handleCustomerStripeException(CustomerStripeException ex, ServerWebExchange exchange) {
        return handleWithMessage(HttpStatus.INTERNAL_SERVER_ERROR, ex, exchange);
    }
}
