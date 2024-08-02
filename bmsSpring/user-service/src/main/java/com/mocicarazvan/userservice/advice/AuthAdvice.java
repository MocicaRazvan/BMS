package com.mocicarazvan.userservice.advice;


import com.mocicarazvan.templatemodule.advices.BaseExceptionMapping;
import com.mocicarazvan.templatemodule.dtos.errors.BaseErrorResponse;
import com.mocicarazvan.templatemodule.exceptions.common.UsernameNotFoundException;
import com.mocicarazvan.userservice.exceptions.EmailAlreadyVerified;
import com.mocicarazvan.userservice.exceptions.StateNotFound;
import com.mocicarazvan.userservice.exceptions.UserWithEmailExists;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class AuthAdvice extends BaseExceptionMapping {


    @ExceptionHandler(UsernameNotFoundException.class)
    public Mono<ResponseEntity<BaseErrorResponse>> notFound(UsernameNotFoundException error, ServerWebExchange exchange) {
        return handleWithMessage(HttpStatus.NOT_FOUND, error, exchange);

    }

    @ExceptionHandler(UserWithEmailExists.class)
    public Mono<ResponseEntity<BaseErrorResponse>> handleUserWithEmailExists(UserWithEmailExists e, ServerWebExchange exchange) {
        return handleWithMessage(HttpStatus.CONFLICT, e, exchange);
    }

    @ExceptionHandler(StateNotFound.class)
    public Mono<ResponseEntity<BaseErrorResponse>> handleStateNotFound(StateNotFound e, ServerWebExchange exchange) {
        return handleWithMessage(HttpStatus.BAD_REQUEST, e, exchange);
    }

    @ExceptionHandler(EmailAlreadyVerified.class)
    public Mono<ResponseEntity<BaseErrorResponse>> handleEmailAlreadyVerified(EmailAlreadyVerified e, ServerWebExchange exchange) {
        return handleWithMessage(HttpStatus.BAD_REQUEST, e, exchange);
    }

}
