package com.mocicarazvan.dayservice.clients;


import com.mocicarazvan.templatemodule.clients.CountInParentClient;
import com.mocicarazvan.templatemodule.dtos.response.EntityCount;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class PlanClient extends CountInParentClient {
    private static final String CLIENT_NAME = "planService";

    @Value("${plan-service.url}")
    private String planServiceUrl;

    public PlanClient(@Qualifier("planWebClient") WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(CLIENT_NAME, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @Override
    public Mono<EntityCount> getCountInParent(Long childId, String userId) {
        // todo change to be good
        return super.getCountInParent(childId, userId, e -> Mono.just(new EntityCount(1L)))
                .log();
//        return Mono.just(new EntityCount(0L));
    }

    @Override
    public WebClient getClient() {
        return webClientBuilder.baseUrl(planServiceUrl + "/plans").build();
    }

    @PostConstruct
    public void init() {
        setServiceUrl(planServiceUrl);
    }


}
