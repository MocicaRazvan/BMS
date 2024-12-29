package com.mocicarazvan.ollamasearch.clients;


import com.mocicarazvan.ollamasearch.config.OllamaPropertiesConfig;
import com.mocicarazvan.ollamasearch.dtos.embed.OllamaEmbedRequestModel;
import com.mocicarazvan.ollamasearch.dtos.embed.OllamaEmbedResponseModel;
import com.mocicarazvan.ollamasearch.exceptions.OllamaEmbedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Component
public class OllamaAPI {
    private final OllamaPropertiesConfig ollamaPropertiesConfig;
    private final String name = "ollamaClient";
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final RateLimiter rateLimiter;
    private final WebClient webClient;

    public OllamaAPI(OllamaPropertiesConfig ollamaPropertiesConfig, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry, @Qualifier("ollamaAPIWebClient") WebClient.Builder webClientBuilder) {
        this.ollamaPropertiesConfig = ollamaPropertiesConfig;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        this.retry = retryRegistry.retry(name);
        this.rateLimiter = rateLimiterRegistry.rateLimiter(name);
        this.webClient = webClientBuilder.baseUrl(handleUrl(ollamaPropertiesConfig)).build();
    }

    public Mono<OllamaEmbedResponseModel> embed(OllamaEmbedRequestModel embedRequestModel) {
        return webClient.post()
                .uri("/api/embed")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(embedRequestModel)
                .retrieve()
                .onRawStatus(status -> status != 200, this::createErrorResponse)
                .bodyToMono(OllamaEmbedResponseModel.class)
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                // bc it can be a long operation
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<? extends Throwable> createErrorResponse(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new OllamaEmbedException(response.statusCode() + " _ " + body)));
    }

    private String handleUrl(OllamaPropertiesConfig ollamaPropertiesConfig) {
        String url;
        if (ollamaPropertiesConfig.getUrl().endsWith("/")) {
            url = ollamaPropertiesConfig.getUrl().substring(0, ollamaPropertiesConfig.getUrl().length() - 1);
        } else {
            url = ollamaPropertiesConfig.getUrl();
        }
        return url;
    }
}
