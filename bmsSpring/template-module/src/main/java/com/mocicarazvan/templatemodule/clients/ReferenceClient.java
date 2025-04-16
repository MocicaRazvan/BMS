package com.mocicarazvan.templatemodule.clients;

import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
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
                .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                .bodyToMono(Void.class)
                .transform(this::applyResilience)
                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Mono.error(new NotFoundEntity(referenceName, Long.valueOf(referenceId))));
    }

    public <T extends ApproveDto> Mono<T> getReferenceById(String referenceId, String userId, Class<T> clazz) {
        if (serviceUrl == null) {
            return Mono.error(new NotFoundEntity(referenceName, Long.valueOf(referenceId)));
        }
        return getItemById(referenceId, userId, new ParameterizedTypeReference<CustomEntityModel<T>>() {
        }, e -> Mono.error(new NotFoundEntity(referenceName, Long.valueOf(referenceId))))
                .map(CustomEntityModel::getContent);
    }
}
