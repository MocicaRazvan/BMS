package com.mocicarazvan.orderservice.clients;


import com.mocicarazvan.orderservice.dtos.notifications.InternalBoughtBody;
import com.mocicarazvan.templatemodule.clients.ClientBase;
import com.mocicarazvan.templatemodule.clients.ClientExceptionHandler;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class BoughtWebSocketClient extends ClientBase {
    private static final String CLIENT_NAME = "websocketService";

    @Value("${websocket-service.url}")
    private String websocketServiceUrl;

    public BoughtWebSocketClient(@Qualifier("webSocketWebClient") WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(CLIENT_NAME, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @Override
    public WebClient getClient() {
        return webClientBuilder.baseUrl(websocketServiceUrl + "/boughtNotification").build();
    }

    public Mono<Void> saveNotifications(InternalBoughtBody internalBoughtBody) {
        if (serviceUrl == null) {
            return Mono.error(new IllegalArgumentException("Service url is null"));
        }
        if (internalBoughtBody == null) {
            return Mono.error(new IllegalArgumentException("InternalBoughtBody is null"));
        }
        if (internalBoughtBody.getPlans().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Plans list is empty"));
        }
        return getClient()
                .patch()
                .uri(uriBuilder -> uriBuilder.path("/internal/sendNotifications").build())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(internalBoughtBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                .bodyToMono(Void.class)
                .transform(this::applyResilience)
                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> {
                    log.error("Error occurred while sending notifications", e);
                    return Mono.empty();
                });
    }

    @PostConstruct
    public void init() {
        setServiceUrl(websocketServiceUrl);
    }
}
