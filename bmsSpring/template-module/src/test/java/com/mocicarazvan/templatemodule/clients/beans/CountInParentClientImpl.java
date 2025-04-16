package com.mocicarazvan.templatemodule.clients.beans;

import com.mocicarazvan.templatemodule.clients.CountInParentClient;
import com.mocicarazvan.templatemodule.dtos.response.EntityCount;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class CountInParentClientImpl extends CountInParentClient {
    public CountInParentClientImpl(String service, WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(service, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @Override
    public Mono<EntityCount> getCountInParent(Long childId, String userId) {
        return super.getCountInParent(childId, userId, e -> Mono.just(new EntityCount(1L)));
    }

    @Override
    public WebClient getClient() {
        return this.webClientBuilder
                .baseUrl(this.serviceUrl)
                .build();
    }
}
