package com.mocicarazvan.planservice.clients;

import com.mocicarazvan.planservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.templatemodule.clients.ValidIdsClient;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class RecipeClient extends ValidIdsClient<RecipeResponse> {
    private static final String CLIENT_NAME = "recipeService";

    @Value("${recipe-service.url}")
    private String recipeServiceUrl;

    public RecipeClient(WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(CLIENT_NAME, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @Override
    public Mono<Void> verifyIds(List<String> ids, String userId) {
        return super.verifyIds(ids, userId, e -> Mono.error(new IllegalArgumentException("Invalid recipes " + ids.toString())));
    }

    @Override
    public Flux<RecipeResponse> getByIds(List<String> ids, String userId) {
        return super.getByIds(ids, userId, RecipeResponse.class, e -> Flux.error(new IllegalArgumentException("Invalid recipes " + ids.toString())));
    }

    public Mono<ResponseWithUserDtoEntity<RecipeResponse>> getByIdWithUser(String id, String userId) {
        ParameterizedTypeReference<ResponseWithUserDtoEntity<RecipeResponse>> typeRef =
                new ParameterizedTypeReference<>() {
                };
        return super.getItemById(id, userId, typeRef, "/internal/withUser", e -> Mono.error(new IllegalArgumentException("Invalid recipe " + id)));
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
