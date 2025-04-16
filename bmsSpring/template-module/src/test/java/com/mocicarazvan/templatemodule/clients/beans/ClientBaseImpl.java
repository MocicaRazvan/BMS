package com.mocicarazvan.templatemodule.clients.beans;

import com.mocicarazvan.templatemodule.clients.ClientBase;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.web.reactive.function.client.WebClient;

public class ClientBaseImpl extends ClientBase {


    public ClientBaseImpl(
            WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super("test-service", webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @Override
    public WebClient getClient() {
        return this.webClientBuilder
                .baseUrl(this.serviceUrl)
                .build();
    }
}
