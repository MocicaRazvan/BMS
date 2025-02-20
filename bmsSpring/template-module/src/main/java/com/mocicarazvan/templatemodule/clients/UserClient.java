package com.mocicarazvan.templatemodule.clients;


import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.exceptions.common.ServiceCallFailedException;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class UserClient extends ClientBase {


    @Value("${user-service.url}")
    private String userServiceUrl;

    public UserClient(String service, WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry,
                      RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(service, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }


    public Mono<UserDto> getUser(String uri, String userId) {
        return getUser(uri + "/" + userId);
    }

    public Mono<UserDto> getUser(String uri) {
        return getClient()
                .get()
                .uri(uri)
                .accept(MediaType.APPLICATION_NDJSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleNotFoundException(response, uri))
                .bodyToMono(new ParameterizedTypeReference<CustomEntityModel<UserDto>>() {
                })
                .transform(this::applyResilience)
                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Mono.error(new NotFoundEntity(
                        "user", Long.valueOf(uri.substring(uri.lastIndexOf("/") + 1))
                )))
                .map(CustomEntityModel::getContent)
                ;


    }

    private Mono<? extends Throwable> handleNotFoundException(ClientResponse response, String uri) {
        log.info(response.toString());
        if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
            return response.bodyToMono(NotFoundEntity.class)
                    .flatMap(Mono::error);
        } else {
            return response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new ServiceCallFailedException(body, "user-service", uri)));
        }
    }

    public Mono<Boolean> hasPermissionToModifyEntity(UserDto authUser, Long entityUserId) {
        return Mono.just(authUser.getRole() == Role.ROLE_ADMIN || Objects.equals(authUser.getId(), entityUserId));
    }

    public Mono<Void> existsUser(String uri, String userId) {
        return existsUser(uri + "/" + userId, List.of(Role.ROLE_TRAINER, Role.ROLE_ADMIN, Role.ROLE_USER));
    }

    public Mono<Void> existsUser(String uri, String trainerId, List<Role> roles) {
        return existsUser(uri + "/" + trainerId, roles);
    }

    // exists/{trainerId}
    public Mono<Void> existsTrainerOrAdmin(String uri, Long trainerId) {
        return existsUser(uri + "/" + trainerId, List.of(Role.ROLE_TRAINER, Role.ROLE_ADMIN));
    }

    public Mono<Void> existsUser(String uri, List<Role> roles) {
        List<Role> rolesList = roles == null ? new ArrayList<>() : roles;
        return getClient()
                .get()
                .uri(uriBuilder -> uriBuilder.path(uri).queryParam("roles", rolesList.stream()
                        .map(Role::toString).toList()
                ).build())
                .accept(MediaType.APPLICATION_NDJSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleNotFoundException(response, uri))
                .bodyToMono(Void.class)
                .transform(this::applyResilience)
                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Mono.error(new NotFoundEntity(
                        "user", Long.valueOf(uri.substring(uri.lastIndexOf("/") + 1))
                )));
    }


    // byIds
    public Flux<UserDto> getUsersByIdIn(String uri, List<Long> ids) {
        return getClient()
                .get()
                .uri(uriBuilder -> {
                    URI builtUri = uriBuilder.path(uri).queryParam("ids", ids).build();
                    log.debug("Calling URI: {}", builtUri);
                    return builtUri;
                })
                .accept(MediaType.APPLICATION_NDJSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleNotFoundException(response, uri))
                .bodyToFlux(new ParameterizedTypeReference<CustomEntityModel<UserDto>>() {
                })
                .transform(this::applyResilience)
                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Flux.empty())
                .map(CustomEntityModel::getContent);
    }


    @Override
    public WebClient getClient() {
        return webClientBuilder.baseUrl(userServiceUrl + "/users").build();
    }
}
