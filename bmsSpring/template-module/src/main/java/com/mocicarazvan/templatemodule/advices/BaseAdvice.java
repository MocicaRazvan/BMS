package com.mocicarazvan.templatemodule.advices;

import com.mocicarazvan.templatemodule.dtos.errors.*;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityNotOwner;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityUsed;
import com.mocicarazvan.templatemodule.exceptions.common.ServiceCallFailedException;
import com.mocicarazvan.templatemodule.exceptions.common.SortingCriteriaException;
import com.mocicarazvan.templatemodule.exceptions.common.UsernameNotFoundException;
import com.mocicarazvan.templatemodule.exceptions.notFound.IdNameException;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundBase;
import com.mocicarazvan.templatemodule.exceptions.notFound.TokenNotFound;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;


public abstract class BaseAdvice extends BaseExceptionMapping {


    @ExceptionHandler(NotFoundBase.class)
    public Mono<ResponseEntity<BaseErrorResponse>> handleBaseNotFound(NotFoundBase e, ServerWebExchange exchange) {
        return handleWithMessage(HttpStatus.NOT_FOUND, e, exchange);
    }

    @ExceptionHandler(IdNameException.class)
    public Mono<ResponseEntity<IdNameResponse>> handleIdNameNotFound(IdNameException exception, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new IdNameResponse().withBase(respWithMessage(HttpStatus.NOT_FOUND, exception, exchange), exception.getName(), exception.getId())
        ));
    }

    @ExceptionHandler(SubEntityUsed.class)
    public Mono<ResponseEntity<IdNameResponse>> handleSubEntityUsed(IdNameException exception, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new IdNameResponse().withBase(respWithMessage(HttpStatus.BAD_REQUEST, exception, exchange), exception.getName(), exception.getId())
        ));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public Mono<ResponseEntity<BaseErrorResponse>> handleUsernameNotFound(UsernameNotFoundException e, ServerWebExchange exchange) {
        return handleWithMessage(HttpStatus.NOT_FOUND, e, exchange);
    }

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

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<ValidationResponse>> handleConstraintValidation(ConstraintViolationException e, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ValidationResponse().withBase(respWithMessage(HttpStatus.BAD_REQUEST, e, exchange), e.getConstraintViolations().stream().collect(
                        HashMap::new,
                        (m, v) -> m.put(v.getPropertyPath().toString(), v.getMessage()),
                        HashMap::putAll
                ))
        ));
    }

    @ExceptionHandler(SortingCriteriaException.class)
    public Mono<ResponseEntity<SortingCriteriaResponse>> handleSortingCriteria(SortingCriteriaException e, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new SortingCriteriaResponse().withBase(respWithMessage(HttpStatus.BAD_REQUEST, e, exchange), e.getInvalidCriteria())
        ));
    }

    @ExceptionHandler({TokenNotFound.class, PrivateRouteException.class})
    public Mono<ResponseEntity<BaseErrorResponse>> handleToken(RuntimeException e, ServerWebExchange exchange) {
        return handleWithMessage(HttpStatus.FORBIDDEN, e, exchange);
    }

    @ExceptionHandler(IllegalActionException.class)
    public Mono<ResponseEntity<BaseErrorResponse>> handleBaseNotFound(IllegalActionException e, ServerWebExchange exchange) {
        return handleWithMessage(HttpStatus.BAD_REQUEST, e, exchange);
    }

    @ExceptionHandler(SubEntityNotOwner.class)
    public Mono<ResponseEntity<SubEntityOwnerResponse>> handleSubEntityNotOwner(SubEntityNotOwner e, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new SubEntityOwnerResponse().withBase(respWithMessage(HttpStatus.FORBIDDEN, e, exchange),
                        e.getEntityUserId(), e.getAuthId(), e.getEntityId())
        ));
    }

    @ExceptionHandler(ServiceCallFailedException.class)
    public Mono<ResponseEntity<ServiceCallFailedExceptionResponse>> handleServiceCallFailed(ServiceCallFailedException e, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ServiceCallFailedExceptionResponse().withBase(respWithMessage(HttpStatus.INTERNAL_SERVER_ERROR, e, exchange),
                        e.getServiceName(), e.getServicePath())
        ));
    }

}
