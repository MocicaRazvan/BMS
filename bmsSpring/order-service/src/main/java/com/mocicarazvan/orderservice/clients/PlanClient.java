package com.mocicarazvan.orderservice.clients;

import com.mocicarazvan.orderservice.dtos.PlanResponse;
import com.mocicarazvan.orderservice.dtos.RecipeResponse;
import com.mocicarazvan.orderservice.enums.DietType;
import com.mocicarazvan.templatemodule.clients.ValidIdsClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class PlanClient extends ValidIdsClient<PlanResponse> {
    private static final String CLIENT_NAME = "planService";

    @Value("${plan-service.url}")
    private String planServiceUrl;

    public PlanClient(WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(CLIENT_NAME, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @Override
    public WebClient getClient() {
        return webClientBuilder.baseUrl(planServiceUrl + "/plans").build();
    }

    @Override
    public Mono<Void> verifyIds(List<String> ids, String userId) {
        return super.verifyIds(ids, userId, e -> Mono.error(new IllegalArgumentException("Invalid plans " + ids.toString())));
    }

    @Override
    public Flux<PlanResponse> getByIds(List<String> ids, String userId) {
        return super.getByIds(ids, userId, PlanResponse.class, e -> Flux.error(new IllegalArgumentException("Invalid plans " + ids.toString())));
    }


    public Flux<PlanResponse> getTrainersPlans(String trainerId, String userId) {
        if (serviceUrl == null) {
            return Flux.error(new IllegalArgumentException("Service url is null"));
        }
        return getClient()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/internal/trainer/{trainerId}").build(trainerId))
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleClientException(response, serviceUrl))
                .bodyToFlux(PlanResponse.class)
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .onErrorResume(WebClientRequestException.class, this::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Flux.empty());
    }

    public Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getPlansForUser(String title, DietType dietType, List<Long> ids, PageableBody pageableBody, String userId) {
        if (serviceUrl == null) {
            return Flux.error(new IllegalArgumentException("Service url is null"));
        }
        if (ids.isEmpty()) {
            return Flux.empty();
        }
        return getClient()
                .patch()
                .uri(uriBuilder -> uriBuilder.path("/internal/filtered/withUser")
                        .queryParam("title", title)
                        .queryParam("type", dietType)
                        .queryParam("ids", ids)
                        .build())
                .bodyValue(pageableBody)
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleClientException(response, serviceUrl))
                .bodyToFlux(new ParameterizedTypeReference<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>>() {
                })
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .onErrorResume(WebClientRequestException.class, this::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Flux.error(new IllegalArgumentException("Invalid plans " + ids)));
    }

    public Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByPlanForUser(String id, String recipeId, String userId) {
        if (serviceUrl == null) {
            return Mono.error(new IllegalArgumentException("Service url is null"));
        }
        return getClient()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/internal/recipes/{id}/{recipeId}").build(id, recipeId))
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleClientException(response, serviceUrl))
                .bodyToMono(new ParameterizedTypeReference<ResponseWithUserDtoEntity<RecipeResponse>>() {
                })
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .onErrorResume(WebClientRequestException.class, this::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Mono.error(new IllegalArgumentException("Invalid plan " + id)));
    }

    public Mono<ResponseWithUserDtoEntity<PlanResponse>> getPlanById(String id, String userId) {
        return super.getItemById(id, userId, new ParameterizedTypeReference<>() {
        }, "/internal/withUser", e -> Mono.error(new IllegalArgumentException("Invalid plan " + id)));
    }

    public Flux<RecipeResponse> getRecipesByPlan(String id, String userId) {
        if (serviceUrl == null) {
            return Flux.error(new IllegalArgumentException("Service url is null"));
        }
        return getClient()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/recipes/{id}").build(id))
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleClientException(response, serviceUrl))
                .bodyToFlux(RecipeResponse.class)
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .onErrorResume(WebClientRequestException.class, this::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Flux.error(new IllegalArgumentException("Invalid plan " + id)));
    }

    @PostConstruct
    public void init() {
        setServiceUrl(planServiceUrl);
    }
}
