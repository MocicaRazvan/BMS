package com.mocicarazvan.templatemodule.clients.beans;

import com.mocicarazvan.templatemodule.clients.ReferenceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.web.reactive.function.client.WebClient;

public class ReferenceClientImpl extends ReferenceClient {
    public ReferenceClientImpl(String service, WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry, String referenceName) {
        super(service, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry, referenceName);
    }
}
