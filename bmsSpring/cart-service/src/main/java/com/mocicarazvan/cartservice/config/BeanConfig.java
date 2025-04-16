package com.mocicarazvan.cartservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.jackson.CustomObjectMapper;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
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

    @Bean(name = "userWebClient")
    @Profile("!k8s")
    @LoadBalanced
    public WebClient.Builder userWebClient() {
        return WebClient.builder();
    }

    @Bean(name = "planWebClient")
    @Profile("!k8s")
    @LoadBalanced
    public WebClient.Builder planClient() {
        return WebClient.builder();
    }

    @Bean("orderWebClient")
    @Profile("!k8s")
    @LoadBalanced
    public WebClient.Builder orderWebClient() {
        return WebClient.builder();
    }


    @Bean(name = "userWebClient")
    @Profile("k8s")
    public WebClient.Builder userWebClientk8s() {
        return WebClient.builder();
    }

    @Bean(name = "planWebClient")
    @Profile("k8s")
    public WebClient.Builder planClientk8s() {
        return WebClient.builder();
    }

    @Bean("orderWebClient")
    @Profile("k8s")
    public WebClient.Builder orderWebClientk8s() {
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
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager txManager) {
        return TransactionalOperator.create(txManager);
    }

    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }
}
