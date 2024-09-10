package com.mocicarazvan.ingredientservice.conifg;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactResponse;
import com.mocicarazvan.ingredientservice.dtos.IngredientResponse;
import com.mocicarazvan.ingredientservice.dtos.NutritionalFactResponse;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCache;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheChildFilterKey;
import com.mocicarazvan.templatemodule.cache.impl.FilteredListCaffeineCacheBaseImpl;
import com.mocicarazvan.templatemodule.cache.impl.FilteredListCaffeineCacheChildFilterKeyImpl;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
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

    @Bean
    @LoadBalanced
    public WebClient.Builder userWebClient() {
        return WebClient.builder();
    }

    @Bean(name = "recipeWebClient")
    @LoadBalanced
    public WebClient.Builder recipeClient() {
        return WebClient.builder();
    }

    @Bean
    public UserClient userClient(
            CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry
    ) {
        return new UserClient("userService", userWebClient(), circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
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
    public FilteredListCaffeineCache<FilterKeyType, IngredientResponse> filterKeyTypeIngredientResponseFilteredListCaffeineCache() {
        return new FilteredListCaffeineCacheBaseImpl<>("ingredient");
    }

    @Bean
    public FilteredListCaffeineCache<FilterKeyType, IngredientNutritionalFactResponse> filterKeyTypeIngredientNutritionalFactResponseFilteredListCaffeineCache() {
        return new FilteredListCaffeineCacheBaseImpl<>("ingredient-nutritionalFact");
    }

    @Bean
    public FilteredListCaffeineCacheChildFilterKey<NutritionalFactResponse> nutritionalFactResponseFilteredListCaffeineCacheChildFilterKey() {
        return new FilteredListCaffeineCacheChildFilterKeyImpl<>("nutritionalFact");
    }
}
