package com.mocicarazvan.recipeservice.clients;

import com.mocicarazvan.recipeservice.dtos.ingredients.IngredientNutritionalFactResponse;
import com.mocicarazvan.recipeservice.dtos.ingredients.IngredientResponse;
import com.mocicarazvan.templatemodule.clients.ValidIdsClient;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class IngredientClient extends ValidIdsClient<IngredientResponse> {
    private static final String CLIENT_NAME = "ingredientService";
    @Value("${ingredient-service.url}")
    private String ingredientServiceUrl;

    public IngredientClient(@Qualifier("ingredientWebClient") WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(CLIENT_NAME, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    public Flux<IngredientResponse> getByIds(List<String> ids, String userId) {
        return getByIds(ids, userId, IngredientResponse.class, e -> Flux.error(new IllegalActionException("Invalid ingredients " + ids.toString())));
    }

    public Mono<IngredientNutritionalFactResponse> getByIdWithInfo(String id, String userId) {
        return getClient()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/internal/{id}").build(id))
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleClientException(response, serviceUrl))
                .bodyToMono(IngredientNutritionalFactResponse.class)
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .onErrorResume(WebClientRequestException.class, this::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Mono.error(new IllegalActionException("Invalid ingredient " + id)));
    }

    public Mono<Void> verifyIds(List<String> ids, String userId) {
        return verifyIds(ids, userId, e -> Mono.error(new IllegalActionException("Invalid ingredients " + ids.toString())));
    }


    @Override
    public WebClient getClient() {
        return webClientBuilder.baseUrl(ingredientServiceUrl + "/ingredients").build();
    }

    @PostConstruct
    public void init() {
        setServiceUrl(ingredientServiceUrl);
    }
}
