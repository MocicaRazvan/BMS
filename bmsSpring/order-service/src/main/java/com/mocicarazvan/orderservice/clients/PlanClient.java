package com.mocicarazvan.orderservice.clients;

import com.mocicarazvan.orderservice.dtos.clients.DayResponse;
import com.mocicarazvan.orderservice.dtos.clients.MealResponse;
import com.mocicarazvan.orderservice.dtos.clients.PlanResponse;
import com.mocicarazvan.orderservice.dtos.clients.RecipeResponse;
import com.mocicarazvan.orderservice.dtos.clients.collect.FullDayResponse;
import com.mocicarazvan.orderservice.enums.DayType;
import com.mocicarazvan.orderservice.enums.DietType;
import com.mocicarazvan.orderservice.enums.ObjectiveType;
import com.mocicarazvan.templatemodule.clients.ClientExceptionHandler;
import com.mocicarazvan.templatemodule.clients.ValidIdsClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class PlanClient extends ValidIdsClient<PlanResponse> {
    private static final String CLIENT_NAME = "planService";

    @Value("${plan-service.url}")
    private String planServiceUrl;

    public PlanClient(@Qualifier("planWebClient") WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
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
                .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                .bodyToFlux(PlanResponse.class)
                .transform(this::applyResilience)
                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Flux.empty());
    }

    public Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getPlansForUser(String title, DietType dietType, ObjectiveType objective, List<Long> ids,
                                                                                           LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                           LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                           PageableBody pageableBody, String userId) {
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
                        .queryParam("objective", objective)
                        .queryParamIfPresent("createdAtLowerBound", Optional.ofNullable(createdAtLowerBound))
                        .queryParamIfPresent("createdAtUpperBound", Optional.ofNullable(createdAtUpperBound))
                        .queryParamIfPresent("updatedAtLowerBound", Optional.ofNullable(updatedAtLowerBound))
                        .queryParamIfPresent("updatedAtUpperBound", Optional.ofNullable(updatedAtUpperBound))
                        .build())
                .bodyValue(pageableBody)
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                .bodyToFlux(new ParameterizedTypeReference<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>>() {
                })
                .transform(this::applyResilience)
                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Flux.error(new IllegalArgumentException("Invalid plans " + ids)));
    }


    public Mono<FullDayResponse> getFullDayByPlanForUser(String id, String dayId, String userId) {
        if (serviceUrl == null) {
            return Mono.error(new IllegalArgumentException("Service url is null"));
        }
        return getBaseMono(
                getClient(),
                userId,
                uriBuilder -> uriBuilder.path("/internal/days/full/{id}/{dayId}").build(id, dayId),
                new ParameterizedTypeReference<>() {
                },
                e -> Mono.error(new IllegalArgumentException("Invalid plan " + id))
        );
    }

//  /internal/days/recipe/{id}/{dayId}/{recipeId}

    public Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByIdWithUser(String planId, String dayId, String recipeId, String userId) {
        return getBaseMono(
                getClient(),
                userId,
                uriBuilder -> uriBuilder.path("/internal/days/recipe/{id}/{dayId}/{recipeId}").build(planId, dayId, recipeId),
                new ParameterizedTypeReference<>() {
                }, e -> Mono.error(new IllegalArgumentException("Invalid recipe " + recipeId)));

    }

    public Flux<CustomEntityModel<MealResponse>> getMealsByDayEntity(String planId, String dayId, String userId) {
        return getBaseFlux(
                getClient(),
                userId,
                uriBuilder -> uriBuilder.path("/internal/days/meals/{id}/{dayId}").build(planId, dayId),
                new ParameterizedTypeReference<>() {
                },
                e -> Flux.error(new IllegalArgumentException("Invalid day " + dayId))
        );
    }

    public Mono<ResponseWithUserDtoEntity<DayResponse>> getDayByIdWithUser(String planId, String dayId, String userId) {
        return getBaseMono(
                getClient(),
                userId,
                uriBuilder -> uriBuilder.path("/internal/days/{id}/{dayId}").build(planId, dayId),
                new ParameterizedTypeReference<>() {
                }, e -> Mono.error(new IllegalArgumentException("Invalid day " + dayId)));
    }


    public Mono<ResponseWithUserDtoEntity<PlanResponse>> getPlanById(String id, String userId) {
        return super.getItemById(id, userId, new ParameterizedTypeReference<>() {
        }, "/internal/withUser", e -> Mono.error(new IllegalArgumentException("Invalid plan " + id)));
    }


    public Flux<FullDayResponse> getFullDaysByPlan(String id, String userId) {
        if (serviceUrl == null) {
            return Flux.error(new IllegalArgumentException("Service url is null"));
        }
        return getBaseFlux(
                getClient(),
                userId,
                uriBuilder -> uriBuilder.path("/internal/days/{id}").build(id),
                new ParameterizedTypeReference<>() {
                },
                e -> Flux.error(new IllegalArgumentException("Invalid plan " + id))
        );
    }

    public Flux<PageableResponse<CustomEntityModel<DayResponse>>> getDaysFilteredByPlanIdsIn(
            List<Long> plans, String title, DayType type, List<Long> excludeIds,
            LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
            LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
            PageableBody pageableBody, String userId, Boolean admin) {
        return getClient()
                .patch()
                .uri(uriBuilder ->
                        uriBuilder.path("/internal/days/filteredByPlanIdsIn")
                                .queryParam("plans", plans)
                                .queryParamIfPresent("title", Optional.ofNullable(title))
                                .queryParamIfPresent("type", Optional.ofNullable(type))
                                .queryParamIfPresent("excludeIds", Optional.ofNullable(excludeIds))
                                .queryParamIfPresent("createdAtLowerBound", Optional.ofNullable(createdAtLowerBound))
                                .queryParamIfPresent("createdAtUpperBound", Optional.ofNullable(createdAtUpperBound))
                                .queryParamIfPresent("updatedAtLowerBound", Optional.ofNullable(updatedAtLowerBound))
                                .queryParamIfPresent("updatedAtUpperBound", Optional.ofNullable(updatedAtUpperBound))
                                .queryParamIfPresent("admin", Optional.ofNullable(admin))
                                .build()
                )
                .bodyValue(pageableBody)
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                .bodyToFlux(new ParameterizedTypeReference<PageableResponse<CustomEntityModel<DayResponse>>>() {
                })
                .transform(this::applyResilience)
                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Flux.empty());
    }

    @PostConstruct
    public void init() {
        setServiceUrl(planServiceUrl);
    }
}
