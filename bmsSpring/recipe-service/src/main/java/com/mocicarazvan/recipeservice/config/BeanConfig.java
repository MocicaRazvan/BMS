package com.mocicarazvan.recipeservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.aspects.RedisReactiveCacheApprovedAspect;
import com.mocicarazvan.rediscache.aspects.RedisReactiveCacheApprovedEvictAspect;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisApprovedCacheUtils;
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.jackson.CustomObjectMapper;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import com.mocicarazvan.templatemodule.utils.RepositoryUtils;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class BeanConfig {


    @Bean
    public ObjectMapper customObjectMapper(final Jackson2ObjectMapperBuilder builder) {
        return new CustomObjectMapper(builder).customObjectMapper();
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

    @Bean(name = "userWebClient")
    @Profile("!k8s")
    @LoadBalanced
    public WebClient.Builder userWebClient() {
        return WebClient.builder();
    }

    @Bean(name = "fileWebClient")
    @Profile("!k8s")
    @LoadBalanced
    public WebClient.Builder fileWebClient() {
        return WebClient.builder();
    }

    @Bean("dayWebClient")
    @Profile("!k8s")
    @LoadBalanced
    public WebClient.Builder dayClient() {
        return WebClient.builder();
    }

    @Bean("ingredientWebClient")
    @Profile("!k8s")
    @LoadBalanced
    public WebClient.Builder ingredientClient() {
        return WebClient.builder();
    }


    @Bean(name = "userWebClient")
    @Profile("k8s")
    public WebClient.Builder userWebClientk8s() {
        return WebClient.builder();
    }

    @Bean(name = "fileWebClient")
    @Profile("k8s")
    public WebClient.Builder fileWebClientk8s() {
        return WebClient.builder();
    }

    @Bean("dayWebClient")
    @Profile("k8s")
    public WebClient.Builder dayClientk8s() {
        return WebClient.builder();
    }

    @Bean("ingredientWebClient")
    @Profile("k8s")
    public WebClient.Builder ingredientClientk8s() {
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
    public FileClient fileClient(
            CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry,
            @Qualifier("fileWebClient") WebClient.Builder fileWebClient
    ) {
        return new FileClient("fileService", fileWebClient, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @Bean
    public Validator localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public RepositoryUtils repositoryUtils() {
        return new RepositoryUtils();
    }

//    @Bean
//    public FilteredListCaffeineCacheApproveFilterKey<RecipeResponse> filteredListCaffeineCacheApproveFilterKey() {
//        return new FilteredListCaffeineCacheApproveFilterKeyImpl<>("recipeService");
//    }

    @Bean
    public RedisApprovedCacheUtils redisApprovedCacheUtils(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                                           AspectUtils aspectUtils) {
        return new RedisApprovedCacheUtils(aspectUtils, reactiveRedisTemplate);
    }

    @Bean
    public RedisReactiveCacheApprovedAspect redisReactiveCacheApprovedAspect(
            ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
            AspectUtils aspectUtils,
            ObjectMapper objectMapper,
            @Qualifier("redisAsyncTaskExecutor") SimpleAsyncTaskExecutor executorService,
            RedisApprovedCacheUtils redisApprovedCacheUtils
    ) {
        return new RedisReactiveCacheApprovedAspect(reactiveRedisTemplate, aspectUtils, objectMapper,
                executorService, redisApprovedCacheUtils);
    }

    @Bean
    public RedisReactiveCacheApprovedEvictAspect redisReactiveCacheApprovedEvictAspect(
            ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
            AspectUtils aspectUtils, RedisApprovedCacheUtils redisApprovedCacheUtils
    ) {
        return new RedisReactiveCacheApprovedEvictAspect(reactiveRedisTemplate, aspectUtils, redisApprovedCacheUtils);
    }
}
