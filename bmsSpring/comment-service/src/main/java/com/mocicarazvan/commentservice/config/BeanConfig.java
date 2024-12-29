package com.mocicarazvan.commentservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.commentservice.clients.PostClient;
import com.mocicarazvan.commentservice.dtos.CommentResponse;
import com.mocicarazvan.commentservice.enums.CommentReferenceType;
import com.mocicarazvan.rediscache.aspects.RedisReactiveCacheChildAspect;
import com.mocicarazvan.rediscache.aspects.RedisReactiveChildCacheEvictAspect;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisChildCacheUtils;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheChildFilterKey;
import com.mocicarazvan.templatemodule.cache.impl.FilteredListCaffeineCacheChildFilterKeyImpl;
import com.mocicarazvan.templatemodule.clients.ReferenceClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.jackson.CustomObjectMapper;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.concurrent.ExecutorService;

@Configuration
public class BeanConfig {


    @Bean
    public ObjectMapper customObjectMapper(final Jackson2ObjectMapperBuilder builder) {
        return new CustomObjectMapper(builder).customObjectMapper();
    }

    @Bean(name = "userWebClient")
    @Profile("!k8s")
    @LoadBalanced
    public WebClient.Builder userWebClient() {
        return WebClient.builder();
    }

    @Bean(name = "postWebClient")
    @Profile("!k8s")
    @LoadBalanced
    public WebClient.Builder postClient() {
        return WebClient.builder();
    }

    @Bean(name = "userWebClient")
    @Profile("k8s")
    public WebClient.Builder userWebClientk8s() {
        return WebClient.builder();
    }

    @Bean(name = "postWebClient")
    @Profile("k8s")
    public WebClient.Builder postClientk8s() {
        return WebClient.builder();
    }

    @Bean
    public UserClient userClient(
            CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry,
            @Qualifier("userWebClient") WebClient.Builder userWebClient
    ) {
        return new UserClient("userService", userWebClient, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
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

    @Bean
    public RedisChildCacheUtils redisChildCacheUtils(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                                     AspectUtils aspectUtils) {
        return new RedisChildCacheUtils(aspectUtils, reactiveRedisTemplate);
    }

    @Bean
    public RedisReactiveCacheChildAspect redisReactiveCacheApprovedAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                                                          AspectUtils aspectUtils,
                                                                          ObjectMapper objectMapper,
                                                                          ExecutorService executorService,
                                                                          RedisChildCacheUtils redisChildUtils) {
        return new RedisReactiveCacheChildAspect(reactiveRedisTemplate, aspectUtils, objectMapper, executorService, redisChildUtils);
    }

    @Bean
    public RedisReactiveChildCacheEvictAspect redisReactiveChildCacheEvictAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                                                                 AspectUtils aspectUtils,
                                                                                 RedisChildCacheUtils redisChildCacheUtils
    ) {
        return new RedisReactiveChildCacheEvictAspect(reactiveRedisTemplate, aspectUtils, redisChildCacheUtils);
    }

}
