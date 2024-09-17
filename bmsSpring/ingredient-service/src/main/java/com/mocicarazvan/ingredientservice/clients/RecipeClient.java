package com.mocicarazvan.ingredientservice.clients;

import com.mocicarazvan.templatemodule.clients.CountInParentClient;
import com.mocicarazvan.templatemodule.dtos.response.EntityCount;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Component
public class RecipeClient extends CountInParentClient {

    private final static String CLIENT_NAME = "recipeService";

    @Value("${recipe-service.url}")
    private String recipeServiceUrl;

    public RecipeClient(@Qualifier("recipeWebClient") WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(CLIENT_NAME, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    public Mono<EntityCount> getCountInParent(Long childId, String userId) {
        return super.getCountInParent(childId, userId, e -> Mono.just(new EntityCount(1L)));
    }


    @Override
    public WebClient getClient() {
        return webClientBuilder.baseUrl(recipeServiceUrl + "/recipes").build();
    }

    @PostConstruct
    public void init() {
        setServiceUrl(recipeServiceUrl);
    }
}
