package com.mocicarazvan.templatemodule.clients;

import com.mocicarazvan.templatemodule.dtos.response.EntityCount;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public abstract class CountInParentClient extends ClientBase {
    public CountInParentClient(String service, WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(service, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    public Mono<EntityCount> getCountInParent(Long childId, String userId, Function<ThrowFallback, Mono<? extends EntityCount>> fallback) {
        if (serviceUrl == null) {
            return Mono.error(new IllegalArgumentException("Service url is null"));
        }
        return getClient()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/internal/count/{childId}").build(childId))
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                .bodyToMono(EntityCount.class)
                .transform(this::applyResilience)
                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, fallback);
    }

    public abstract Mono<EntityCount> getCountInParent(Long childId, String userId);
}
