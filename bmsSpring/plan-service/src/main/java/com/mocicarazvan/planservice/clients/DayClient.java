package com.mocicarazvan.planservice.clients;

import com.mocicarazvan.planservice.dtos.dayClient.DayResponse;
import com.mocicarazvan.planservice.dtos.dayClient.MealResponse;
import com.mocicarazvan.planservice.dtos.dayClient.RecipeResponse;
import com.mocicarazvan.planservice.enums.DayType;
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
public class DayClient extends ValidIdsClient<DayResponse> {
    private static final String CLIENT_NAME = "dayService";

    @Value("${day-service.url}")
    private String dayServiceUrl;

    public DayClient(@Qualifier("dayWebClient") WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(CLIENT_NAME, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @Override
    public Mono<Void> verifyIds(List<String> ids, String userId) {
        return super.verifyIds(ids, userId, e -> Mono.error(new IllegalArgumentException("Invalid days " + ids.toString())));
    }

    @Override
    public Flux<DayResponse> getByIds(List<String> ids, String userId) {
        return super.getByIds(ids, userId, DayResponse.class, e -> Flux.error(new IllegalArgumentException("Invalid days " + ids.toString())));
    }

    public Mono<ResponseWithUserDtoEntity<DayResponse>> getByIdWithUser(String id, String userId) {
        ParameterizedTypeReference<ResponseWithUserDtoEntity<DayResponse>> typeRef =
                new ParameterizedTypeReference<>() {
                };
        return super.getItemById(id, userId, typeRef, "/internal/withUser", e -> Mono.error(new IllegalArgumentException("Invalid day " + id)));
    }

    public Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByIdWithUser(String dayId, String recipeId, String userId) {
        return getBaseMono(
                getClient(),
                userId,
                uriBuilder -> uriBuilder.path("/internal/recipe/{id}/{recipeId}").build(dayId, recipeId),
                new ParameterizedTypeReference<>() {
                }, e -> Mono.error(new IllegalArgumentException("Invalid recipe " + recipeId)));

    }

    public Flux<MealResponse> getMealsByDay(String dayId, String userId) {

        return getBaseFlux(
                getMealsClient(), userId, uriBuilder -> uriBuilder.path("/internal/day/{dayId}").build(dayId),
                new ParameterizedTypeReference<>() {
                }, e -> Flux.empty());

    }

    public Flux<CustomEntityModel<MealResponse>> getMealsByDayEntity(String dayId, String userId) {

        return getBaseFlux(
                getMealsClient(), userId, uriBuilder -> uriBuilder.path("/internal/entity/day/{dayId}").build(dayId),
                new ParameterizedTypeReference<>() {
                }, e -> Flux.empty());

    }

    public Flux<RecipeResponse> getRecipesByMeal(String mealId, String userId) {
        return getBaseFlux(
                getMealsClient(), userId, uriBuilder -> uriBuilder.path("/internal/recipes/{mealId}").build(mealId),
                new ParameterizedTypeReference<>() {
                }, e -> Flux.empty());
    }

    public Flux<PageableResponse<CustomEntityModel<DayResponse>>> getDaysFilteredByIds(String title, DayType type, List<Long> ids, List<Long> excludeIds,
                                                                                       LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                       LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                       PageableBody pageableBody, String userId, Boolean admin) {
        return getClient()
                .patch()
                .uri(uriBuilder ->
                        uriBuilder.path("/internal/filtered/byIds")
                                .queryParamIfPresent("title", Optional.ofNullable(title))
                                .queryParamIfPresent("type", Optional.ofNullable(type))
                                .queryParam("ids", ids)
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
                .onErrorResume(ThrowFallback.class, _ -> Flux.empty());

    }

    public WebClient getMealsClient() {
        return webClientBuilder.baseUrl(dayServiceUrl + "/meals").build();
    }


    @Override
    public WebClient getClient() {
        return webClientBuilder.baseUrl(dayServiceUrl + "/days").build();
    }

    @PostConstruct
    public void init() {
        setServiceUrl(dayServiceUrl);
    }
}
