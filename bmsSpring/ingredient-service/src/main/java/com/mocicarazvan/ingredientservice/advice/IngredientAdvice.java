package com.mocicarazvan.ingredientservice.advice;


import com.mocicarazvan.ingredientservice.dtos.errors.NameAlreadyExistsResponse;
import com.mocicarazvan.ingredientservice.exceptions.NameAlreadyExists;
import com.mocicarazvan.templatemodule.advices.BaseAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class IngredientAdvice extends BaseAdvice {

    @ExceptionHandler(NameAlreadyExists.class)
    public Mono<ResponseEntity<NameAlreadyExistsResponse>> handleNameAlreadyExists(NameAlreadyExists exception, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        new NameAlreadyExistsResponse()
                                .withBase(respWithMessage(HttpStatus.CONFLICT, exception, exchange), exception)
                )
        );
    }
}
