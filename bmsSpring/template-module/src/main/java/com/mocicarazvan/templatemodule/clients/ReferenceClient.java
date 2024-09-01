package com.mocicarazvan.templatemodule.clients;

import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.Setter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Setter
public abstract class ReferenceClient extends ClientBase {
    private final String referenceName;
//    private String serviceUrl;

    public ReferenceClient(String service, WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry, String referenceName) {
        super(service, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
        this.referenceName = referenceName;
    }

    @Override
    public WebClient getClient() {
        return webClientBuilder.baseUrl(serviceUrl + "/" + referenceName + "s").build();
    }

    public Mono<Void> existsApprovedReference(String referenceId) {
        if (serviceUrl == null) {
            return Mono.error(new NotFoundEntity(referenceName, Long.valueOf(referenceId)));
        }
        return getClient()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/internal/existsApproved/{referenceId}").build(referenceId))
                .accept(MediaType.APPLICATION_NDJSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleClientException(response, serviceUrl))
                .bodyToMono(Void.class)
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .onErrorResume(WebClientRequestException.class, this::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Mono.error(new NotFoundEntity(referenceName, Long.valueOf(referenceId))));
    }

//    public <T extends ApproveDto> Mono<T> getReferenceById(String referenceId, String userId, Class<T> clazz) {
//        if (serviceUrl == null) {
//            return Mono.error(new NotFoundEntity(referenceName, Long.valueOf(referenceId)));
//        }
//        return getClient()
//                .get()
//                .uri(uriBuilder -> uriBuilder.path("/{referenceId}").build(referenceId))
//                .accept(MediaType.APPLICATION_NDJSON)
//                .header(RequestsUtils.AUTH_HEADER, userId)
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, response -> handleClientException(response, serviceUrl))
//                .bodyToMono(clazz)
//                .transformDeferred(RetryOperator.of(retry))
//                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
//                .transformDeferred(RateLimiterOperator.of(rateLimiter))
//                .onErrorResume(WebClientRequestException.class, this::handleWebRequestException)
//                .onErrorResume(ThrowFallback.class, e -> Mono.error(new NotFoundEntity(referenceName, Long.valueOf(referenceId))));
//    }

    public <T extends ApproveDto> Mono<T> getReferenceById(String referenceId, String userId, Class<T> clazz) {
        if (serviceUrl == null) {
            return Mono.error(new NotFoundEntity(referenceName, Long.valueOf(referenceId)));
        }
        return getItemById(referenceId, userId, new ParameterizedTypeReference<CustomEntityModel<T>>() {
        }, e -> Mono.error(new NotFoundEntity(referenceName, Long.valueOf(referenceId))))
                .map(CustomEntityModel::getContent);
    }
}
