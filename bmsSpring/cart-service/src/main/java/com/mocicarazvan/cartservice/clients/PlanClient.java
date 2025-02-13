package com.mocicarazvan.cartservice.clients;

import com.mocicarazvan.cartservice.dtos.clients.PlanResponse;
import com.mocicarazvan.templatemodule.clients.ValidIdsClient;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class PlanClient extends ValidIdsClient<PlanResponse> {
    private static final String CLIENT_NAME = "planService";

    @Value("${plan-service.url}")
    private String planServiceUrl;

    public PlanClient(@Qualifier("planWebClient") WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(CLIENT_NAME, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @PostConstruct
    public void init() {
        setServiceUrl(planServiceUrl);
    }

    @Override
    public Mono<Void> verifyIds(List<String> ids, String userId) {
        return super.verifyIds(ids, userId, e -> Mono.error(new IllegalArgumentException("Invalid plans " + ids.toString())));
    }

    @Override
    public Flux<PlanResponse> getByIds(List<String> ids, String userId) {
        return super.getByIds(ids, userId, PlanResponse.class, e -> Flux.error(new IllegalArgumentException("Invalid plans " + ids.toString())));
    }

    @Override
    public WebClient getClient() {
        return webClientBuilder.baseUrl(planServiceUrl + "/plans").build();
    }
}
