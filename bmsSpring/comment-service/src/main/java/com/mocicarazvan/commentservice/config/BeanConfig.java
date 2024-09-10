package com.mocicarazvan.commentservice.config;

import com.mocicarazvan.commentservice.clients.PostClient;
import com.mocicarazvan.commentservice.dtos.CommentResponse;
import com.mocicarazvan.commentservice.enums.CommentReferenceType;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheApproveFilterKey;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheChildFilterKey;
import com.mocicarazvan.templatemodule.cache.impl.FilteredListCaffeineCacheApproveFilterKeyImpl;
import com.mocicarazvan.templatemodule.cache.impl.FilteredListCaffeineCacheChildFilterKeyImpl;
import com.mocicarazvan.templatemodule.clients.ReferenceClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.jackson.CustomObjectMapper;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Configuration
public class BeanConfig {

//    @Value("${user-service.url}")
//    private String userServiceUrl;

    @Bean
    public ObjectMapper customObjectMapper(final Jackson2ObjectMapperBuilder builder) {
        return new CustomObjectMapper(builder).customObjectMapper();
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder userWebClient() {
        return WebClient.builder();
    }

    @Bean(name = "postWebClient")
    @LoadBalanced
    public WebClient.Builder postClient() {
        return WebClient.builder();
    }

    @Bean
    public UserClient userClient(
            CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry
    ) {
        return new UserClient("userService", userWebClient(), circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @Bean
    public RequestsUtils requestsUtils() {
        return new RequestsUtils();
    }

    @Bean
    public EntitiesUtils entitiesUtils() {
        return new EntitiesUtils();
    }

    @Bean
    public PageableUtilsCustom pageableUtilsCustom() {
        return new PageableUtilsCustom();
    }

    @Bean
    public Map<CommentReferenceType, ReferenceClient> referenceClients(
            PostClient postClient
    ) {
        return Map.of(CommentReferenceType.POST, postClient);
    }

    @Bean
    public FilteredListCaffeineCacheChildFilterKey<CommentResponse> commentResponseFilteredListCaffeineCacheChildFilterKey() {
        return new FilteredListCaffeineCacheChildFilterKeyImpl<>("commentService");
    }

}
