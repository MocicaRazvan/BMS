package com.mocicarazvan.cartservice.clients;

import com.mocicarazvan.cartservice.dtos.clients.UserSubscriptionDto;
import com.mocicarazvan.templatemodule.clients.ClientBase;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class OrderClient extends ClientBase {
    private static final String CLIENT_NAME = "orderService";

    @Value("${order-service.url}")
    private String orderServiceUrl;

    public OrderClient(@Qualifier("orderWebClient") WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(CLIENT_NAME, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @Override
    public WebClient getClient() {
        return webClientBuilder.baseUrl(orderServiceUrl + "/orders").build();
    }

    public Flux<UserSubscriptionDto> getUserSubscriptions(String userId) {
        return getBaseFlux(
                getClient(),
                userId,
                uriBuilder -> uriBuilder.path("/subscriptions").build(),
                new ParameterizedTypeReference<>() {
                },
                e -> Flux.empty()
        );
    }


    @PostConstruct
    public void init() {
        setServiceUrl(orderServiceUrl);
    }
}
