package com.mocicarazvan.orderservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.aspects.RedisReactiveCacheChildAspect;
import com.mocicarazvan.rediscache.aspects.RedisReactiveChildCacheEvictAspect;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisChildCacheUtils;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.email.EmailMXCacher;
import com.mocicarazvan.templatemodule.email.EmailUtils;
import com.mocicarazvan.templatemodule.email.config.CustomMailProps;
import com.mocicarazvan.templatemodule.email.config.MailConfig;
import com.mocicarazvan.templatemodule.email.config.SecretProperties;
import com.mocicarazvan.templatemodule.email.impl.EmailUtilsImpl;
import com.mocicarazvan.templatemodule.jackson.CustomObjectMapper;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import com.mocicarazvan.templatemodule.utils.RepositoryUtils;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
@RequiredArgsConstructor
public class BeanConfig {

    private final LocalReactiveCache localReactiveCache;
    private final ReverseKeysLocalCache reverseKeysLocalCache;

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

    @Bean(name = "webSocketWebClient")
    @Profile("!k8s")
    @LoadBalanced
    public WebClient.Builder webSocketClient() {
        return WebClient.builder();
    }

    @Bean(name = "planWebClient")
    @Profile("!k8s")
    @LoadBalanced
    public WebClient.Builder planClient() {
        return WebClient.builder();
    }


    @Bean(name = "userWebClient")
    @Profile("k8s")
    public WebClient.Builder userWebClientk8s() {
        return WebClient.builder();
    }

    @Bean(name = "webSocketWebClient")
    @Profile("k8s")
    public WebClient.Builder webSocketClientk8s() {
        return WebClient.builder();
    }

    @Bean(name = "planWebClient")
    @Profile("k8s")
    public WebClient.Builder planClientk8s() {
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
    public Validator localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public RepositoryUtils repositoryUtils() {
        return new RepositoryUtils();
    }

    @Bean
    public JavaMailSender javaMailSender(CustomMailProps customMailProps, SecretProperties secretProperties) throws Exception {
        return new MailConfig<>(customMailProps, secretProperties).javaMailSender();
    }

    @Bean
    public EmailUtils emailUtils(JavaMailSender jml, EmailMXCacher emailMXCacher) {
        return new EmailUtilsImpl(jml, emailMXCacher);
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
                                                                          @Qualifier("redisAsyncTaskExecutor") SimpleAsyncTaskExecutor executorService,
                                                                          RedisChildCacheUtils redisChildUtils
    ) {
        return new RedisReactiveCacheChildAspect(reactiveRedisTemplate, aspectUtils, objectMapper, executorService, redisChildUtils,
                reverseKeysLocalCache, localReactiveCache);

    }

    @Bean
    public RedisReactiveChildCacheEvictAspect redisReactiveChildCacheEvictAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                                                                 AspectUtils aspectUtils,
                                                                                 RedisChildCacheUtils redisChildCacheUtils,
                                                                                 @Qualifier("redisAsyncTaskExecutor") SimpleAsyncTaskExecutor asyncTaskExecutor
    ) {
        return new RedisReactiveChildCacheEvictAspect(reactiveRedisTemplate, aspectUtils, redisChildCacheUtils,
                reverseKeysLocalCache, localReactiveCache, asyncTaskExecutor);
    }

    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager txManager) {

        return TransactionalOperator.create(txManager);
    }

}
