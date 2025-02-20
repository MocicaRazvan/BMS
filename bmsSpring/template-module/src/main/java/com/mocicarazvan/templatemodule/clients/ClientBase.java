package com.mocicarazvan.templatemodule.clients;


import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
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

    public <T> Flux<T> getBaseFlux(WebClient webClient, String userId, Function<UriBuilder, URI> uriFunction, ParameterizedTypeReference<T> typeRef, Function<ThrowFallback, Flux<? extends T>> fallback) {
        if (serviceUrl == null) {
            return Flux.error(new IllegalArgumentException("Service url is null"));
        }
        return webClient
                .get()
                .uri(uriFunction)
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                .bodyToFlux(typeRef)
                .transform(this::applyResilience)
                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, fallback);
    }

    public <T> Mono<T> getBaseMono(WebClient webClient, String userId, Function<UriBuilder, URI> uriFunction, ParameterizedTypeReference<T> typeRef, Function<ThrowFallback, Mono<? extends T>> fallback) {
        if (serviceUrl == null) {
            return Mono.error(new IllegalArgumentException("Service url is null"));
        }
        return webClient
                .get()
                .uri(uriFunction)
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                .bodyToMono(typeRef)
                .transform(this::applyResilience)
                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, fallback);
    }

    public <T> Mono<T> getItemById(String id, String userId, Class<T> clazz, Function<ThrowFallback, Mono<? extends T>> fallback) {


        return getItemById(id, userId, clazz, "", fallback);
    }

    public <T> Mono<T> getItemById(String id, String userId, ParameterizedTypeReference<T> typeRef, Function<ThrowFallback, Mono<? extends T>> fallback) {


        return getItemById(id, userId, typeRef, "", fallback);
    }

    public <T> Mono<T> getItemById(String id, String userId, Class<T> clazz, String path, Function<ThrowFallback, Mono<? extends T>> fallback) {

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
                .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                .bodyToMono(typeRef)
                .transform(this::applyResilience)
                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, fallback);
    }

    protected <A> Mono<A> applyResilience(Mono<A> m) {
        return m.transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter));
    }

    protected <A> Flux<A> applyResilience(Flux<A> f) {
        return f.transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter));
    }


    public abstract WebClient getClient();
}
