package com.mocicarazvan.templatemodule.clients.beans;

import com.mocicarazvan.templatemodule.clients.ValidIdsClient;
import com.mocicarazvan.templatemodule.dtos.WithUserDtoImpl;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class ValidIdsClientImpl
        extends ValidIdsClient<WithUserDtoImpl> {
    public ValidIdsClientImpl(String service, WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(service, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @Override
    public Mono<Void> verifyIds(List<String> ids, String userId) {
        return super.verifyIds(ids, userId, e -> Mono.error(new IllegalArgumentException("Invalid days " + ids.toString())));

    }

    @Override
    public Flux<WithUserDtoImpl> getByIds(List<String> ids, String userId) {
        return super.getByIds(ids, userId, WithUserDtoImpl.class, e -> Flux.error(new IllegalArgumentException("Invalid days " + ids.toString())));

    }

    @Override
    public WebClient getClient() {
        return this.webClientBuilder
                .baseUrl(this.serviceUrl)
                .build();
    }
}
