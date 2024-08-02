package com.mocicarazvan.templatemodule.clients;


import com.mocicarazvan.templatemodule.dtos.errors.BaseErrorResponse;
import com.mocicarazvan.templatemodule.dtos.errors.IdNameResponse;
import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.exceptions.common.ServiceCallFailedException;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Setter
public abstract class ClientBase {


    protected final String service;
    protected final WebClient.Builder webClientBuilder;
    protected final CircuitBreaker circuitBreaker;
    protected final Retry retry;
    protected final RateLimiter rateLimiter;
    protected String serviceUrl;

    public ClientBase(String service, WebClient.Builder webClientBuilder,
                      CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        this.service = service;
        this.webClientBuilder = webClientBuilder;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(service);
        this.retry = retryRegistry.retry(service);
        this.rateLimiter = rateLimiterRegistry.rateLimiter(service);

    }

    public <T> Mono<T> getItemById(String id, String userId, Class<T> clazz, Function<ThrowFallback, Mono<? extends T>> fallback) {
//        if (serviceUrl == null) {
//            return Mono.error(new IllegalArgumentException("Service url is null"));
//        }
//        return getClient()
//                .get()
//                .uri(uriBuilder -> uriBuilder.path("/{id}").build(id))
//                .accept(MediaType.APPLICATION_NDJSON)
//                .header(RequestsUtils.AUTH_HEADER, userId)
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, response -> handleClientException(response, serviceUrl))
//                .bodyToMono(clazz)
//                .transformDeferred(RetryOperator.of(retry))
//                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
//                .transformDeferred(RateLimiterOperator.of(rateLimiter))
//                .onErrorResume(WebClientRequestException.class, this::handleWebRequestException)
//                .onErrorResume(ThrowFallback.class, fallback);

        return getItemById(id, userId, clazz, "", fallback);
    }

    public <T> Mono<T> getItemById(String id, String userId, Class<T> clazz, String path, Function<ThrowFallback, Mono<? extends T>> fallback) {
//        if (serviceUrl == null) {
//            return Mono.error(new IllegalArgumentException("Service url is null"));
//        }
//
//        return getClient()
//                .get()
//                .uri(uriBuilder -> uriBuilder.path(path + "/{id}").build(id))
//                .accept(MediaType.APPLICATION_NDJSON)
//                .header(RequestsUtils.AUTH_HEADER, userId)
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, response -> handleClientException(response, serviceUrl))
//                .bodyToMono(clazz)
//                .transformDeferred(RetryOperator.of(retry))
//                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
//                .transformDeferred(RateLimiterOperator.of(rateLimiter))
//                .onErrorResume(WebClientRequestException.class, this::handleWebRequestException)
//                .onErrorResume(ThrowFallback.class, fallback);
        return getItemById(id, userId, ParameterizedTypeReference.forType(clazz), path, fallback);

    }

    public <T> Mono<T> getItemById(String id, String userId, ParameterizedTypeReference<T> typeRef, String path, Function<ThrowFallback, Mono<? extends T>> fallback) {
        if (serviceUrl == null) {
            return Mono.error(new IllegalArgumentException("Service url is null"));
        }

        return getClient()
                .get()
                .uri(uriBuilder -> uriBuilder.path(path + "/{id}").build(id))
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleClientException(response, serviceUrl))
                .bodyToMono(typeRef)
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .onErrorResume(WebClientRequestException.class, this::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, fallback);
    }


    protected Mono<? extends Throwable> handleClientException(ClientResponse response, String uri) {
        log.error("Status code: {}, uri: {}", response.statusCode(), uri);
        if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
            return response
                    .bodyToMono(IdNameResponse.class)
                    .log()
                    .flatMap(idNameResponse -> Mono.error(new NotFoundEntity(idNameResponse.getName(), idNameResponse.getId())));
        } else if (response.statusCode().equals(HttpStatus.FORBIDDEN)) {
            return response.bodyToMono(BaseErrorResponse.class)
                    .flatMap(baseErrorResponse -> Mono.error(new PrivateRouteException()));
        } else if (response.statusCode().equals(HttpStatus.BAD_REQUEST)) {
            return response.bodyToMono(BaseErrorResponse.class)
                    .flatMap(baseErrorResponse -> Mono.error(new IllegalActionException(baseErrorResponse.getMessage())));
        } else if (response.statusCode().equals(HttpStatus.SERVICE_UNAVAILABLE) || response.statusCode().is5xxServerError()) {
            return Mono.error(new ThrowFallback());
        } else {
            return response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new ServiceCallFailedException(body, service, uri)));
        }


    }

    protected <T> Mono<T> handleWebRequestException(Throwable e) {
        log.error("Error: ", e);
        return Mono.error(new ThrowFallback());
    }


    public abstract WebClient getClient();
}
