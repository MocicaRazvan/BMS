package com.mocicarazvan.templatemodule.clients;

import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

public abstract class ValidIdsClient<R extends WithUserDto> extends ClientBase {
    public ValidIdsClient(String service, WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(service, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    public Mono<Void> verifyIds(List<String> ids, String userId, Function<ThrowFallback, Mono<? extends Void>> fallback) {
        if (serviceUrl == null) {
            return Mono.error(new IllegalArgumentException("Service url is null"));
        }
        if (ids.isEmpty()) {
            return Mono.empty();
        }
        return getClient()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/internal/validIds").queryParam("ids", ids.stream().distinct().toList()).build())
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleClientException(response, serviceUrl))
                .bodyToMono(Void.class)
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .onErrorResume(WebClientRequestException.class, this::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, fallback);
    }

    public abstract Mono<Void> verifyIds(List<String> ids, String userId);

    public Flux<R> getByIds(List<String> ids, String userId, Class<R> clazz, Function<ThrowFallback, Flux<? extends R>> fallback) {
        if (serviceUrl == null) {
            return Flux.error(new IllegalArgumentException("Service url is null"));
        }
        return getClient()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/internal/getByIds").queryParam("ids", ids).build())
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleClientException(response, serviceUrl))
                .bodyToFlux(clazz)
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .onErrorResume(WebClientRequestException.class, this::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, fallback);
    }

    public abstract Flux<R> getByIds(List<String> ids, String userId);


}
