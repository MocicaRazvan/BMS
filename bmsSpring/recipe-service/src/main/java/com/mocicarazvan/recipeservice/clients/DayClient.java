package com.mocicarazvan.recipeservice.clients;


import com.mocicarazvan.templatemodule.clients.CountInParentClient;
import com.mocicarazvan.templatemodule.dtos.response.EntityCount;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class DayClient extends CountInParentClient {
    private static final String CLIENT_NAME = "dayService";

    @Value("${day-service.url}")
    private String dayServiceUrl;

    public DayClient(WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(CLIENT_NAME, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @Override
    // todo CHANGE TO BE GOOD
    public Mono<EntityCount> getCountInParent(Long childId, String userId) {
        log.error("GET COUNT IN PARENT");
        return super.getCountInParent(childId, userId, e -> Mono.just(new EntityCount(1L)))
                .log();
//        return Mono.just(new EntityCount(0L));
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
