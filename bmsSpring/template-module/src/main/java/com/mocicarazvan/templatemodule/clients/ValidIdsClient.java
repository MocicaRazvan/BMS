package com.mocicarazvan.templatemodule.clients;

import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        return
                Flux.fromIterable(ids)
                        .collect(Collectors.toSet())
                        .flatMap(uIds ->
                                getClient()
                                        .get()
                                        .uri(uriBuilder -> uriBuilder.path("/internal/validIds").queryParam("ids", uIds).build())
                                        .accept(MediaType.APPLICATION_NDJSON)
                                        .header(RequestsUtils.AUTH_HEADER, userId)
                                        .retrieve()
                                        .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                                        .bodyToMono(Void.class)
                                        .transform(this::applyResilience)
                                        .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                                        .onErrorResume(ThrowFallback.class, fallback));
    }

    public abstract Mono<Void> verifyIds(List<String> ids, String userId);

    public Flux<R> getByIds(List<String> ids, String userId, Class<R> clazz, Function<ThrowFallback, Flux<? extends R>> fallback) {
        if (serviceUrl == null) {
            return Flux.error(new IllegalArgumentException("Service url is null"));
        }
        if (ids.isEmpty()) {
            return Flux.empty();
        }
        return
                Flux.fromIterable(ids)
                        .collect(Collectors.toSet())
                        .flatMapMany(uIds ->
                                getClient()
                                        .get()
                                        .uri(uriBuilder -> uriBuilder.path("/internal/getByIds").queryParam("ids", uIds).build())
                                        .accept(MediaType.APPLICATION_NDJSON)
                                        .header(RequestsUtils.AUTH_HEADER, userId)
                                        .retrieve()
                                        .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                                        .bodyToFlux(clazz)
                                        .transform(this::applyResilience)
                                        .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                                        .onErrorResume(ThrowFallback.class, fallback));
    }

    public abstract Flux<R> getByIds(List<String> ids, String userId);


}
