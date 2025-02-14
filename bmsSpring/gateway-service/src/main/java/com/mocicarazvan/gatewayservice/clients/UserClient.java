package com.mocicarazvan.gatewayservice.clients;

import com.mocicarazvan.gatewayservice.dtos.token.TokenValidationRequest;
import com.mocicarazvan.gatewayservice.dtos.token.TokenValidationResponse;
import com.mocicarazvan.gatewayservice.exceptions.ThrowFallback;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class UserClient {


    @Value("${user-service.url}")
    private String userServiceUrl;

    private static final String USER_SERVICE = "userService";
    private final WebClient.Builder userClient;

    protected final CircuitBreaker circuitBreaker;
    protected final Retry retry;
    protected final RateLimiter rateLimiter;

    public UserClient(WebClient.Builder userClient,
                      CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        this.userClient = userClient;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(USER_SERVICE);
        this.retry = retryRegistry.retry(USER_SERVICE);
        this.rateLimiter = rateLimiterRegistry.rateLimiter(USER_SERVICE);
    }

    public Mono<TokenValidationResponse> validateToken(TokenValidationRequest request) {
        return userClient
                .baseUrl(userServiceUrl)
                .build()
                .post()
                .uri("/auth/validateToken")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new ThrowFallback()))
                .bodyToMono(TokenValidationResponse.class)
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .onErrorResume(WebClientRequestException.class, e -> {
                    log.error("Error: ", e);
                    return Mono.error(new ThrowFallback());
                })
                .onErrorResume(ThrowFallback.class, e -> Mono.just(
                        TokenValidationResponse.builder().valid(false).build()
                ));
    }
}
