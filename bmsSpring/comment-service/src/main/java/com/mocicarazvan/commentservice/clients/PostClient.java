package com.mocicarazvan.commentservice.clients;

import com.mocicarazvan.templatemodule.clients.ReferenceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


@Component
public class PostClient extends ReferenceClient {

    private final static String CLIENT_NAME = "postService";
    @Value("${post-service.url}")
    private String postServiceUrl;

    public PostClient(@Qualifier("postWebClient") WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(CLIENT_NAME, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry, "post");
    }

    @PostConstruct
    public void init() {
        setServiceUrl(postServiceUrl);
    }
}
