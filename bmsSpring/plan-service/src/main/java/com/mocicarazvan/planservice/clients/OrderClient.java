package com.mocicarazvan.planservice.clients;

import com.mocicarazvan.templatemodule.clients.CountInParentClient;
import com.mocicarazvan.templatemodule.dtos.response.EntityCount;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OrderClient extends CountInParentClient {
    private static final String CLIENT_NAME = "orderService";

    @Value("${order-service.url}")
    private String orderServiceUrl;

    public OrderClient(WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(CLIENT_NAME, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @Override
    public WebClient getClient() {
        return webClientBuilder.baseUrl(orderServiceUrl + "/orders").build();
    }

    @Override
    //todo change to be good
    public Mono<EntityCount> getCountInParent(Long childId, String userId) {
        return super.getCountInParent(childId, userId, e -> Mono.just(new EntityCount(1L)));
//        return Mono.just(new EntityCount(0L));
    }

    @PostConstruct
    public void init() {
        setServiceUrl(orderServiceUrl);
    }
}
