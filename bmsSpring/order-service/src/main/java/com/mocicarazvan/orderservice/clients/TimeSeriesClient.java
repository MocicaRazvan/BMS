package com.mocicarazvan.orderservice.clients;

import com.mocicarazvan.orderservice.dtos.clients.TimeSeriesRequest;
import com.mocicarazvan.orderservice.dtos.clients.TimeSeriesResponse;
import com.mocicarazvan.orderservice.dtos.summaries.MonthlyOrderSummary;
import com.mocicarazvan.templatemodule.clients.ClientExceptionHandler;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Setter
@Component
public class TimeSeriesClient {
    private final WebClient webClient;
    private static final String CLIENT_NAME = "timeSeries";
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final RateLimiter rateLimiter;
    private final String serviceUrl;

    public TimeSeriesClient(
            @Value("${time-series.url}") String serviceUrl,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            RateLimiterRegistry rateLimiterRegistry) {

        this.webClient = WebClient.builder().baseUrl(serviceUrl).build();
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CLIENT_NAME);
        this.retry = retryRegistry.retry(CLIENT_NAME);
        this.rateLimiter = rateLimiterRegistry.rateLimiter(CLIENT_NAME);
        this.serviceUrl = serviceUrl;
    }

    public Mono<TimeSeriesResponse> getCountAmountPredictions(Flux<MonthlyOrderSummary> monthlyOrderSummary, int predictionLength) {

        return monthlyOrderSummary.reduce(TimeSeriesRequest.builder()
                                .prediction_length(predictionLength)
                                .build(),
                        (TimeSeriesRequest acc, MonthlyOrderSummary cur) -> {
                            acc.addCount(cur.getCount());
                            acc.addAmount(cur.getTotalAmount());
                            return acc;
                        })
//                .log()
                .flatMap(request ->
                        webClient
                                .patch()
                                .uri("/countAmount")
                                .accept(MediaType.APPLICATION_JSON)
                                .bodyValue(request)
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, CLIENT_NAME))
                                .bodyToMono(TimeSeriesResponse.class)
                                .transformDeferred(RetryOperator.of(retry))
                                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                                .onErrorResume(ThrowFallback.class, e -> Mono.error(new IllegalArgumentException("Invalid time series request")))

                );
    }


}
