package com.mocicarazvan.orderservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.clients.UserClient;
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
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mail.javamail.JavaMailSender;
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

    @Bean(name = "webSocketWebClient")
    @LoadBalanced
    public WebClient.Builder webSocketClient() {
        return WebClient.builder();
    }

    @Bean(name = "planWebClient")
    @LoadBalanced
    public WebClient.Builder planClient() {
        return WebClient.builder();
    }

    @Bean
    public UserClient userClient(
            CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry
    ) {
        return new UserClient("userService", userWebClient(), circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

//    @Bean
//    public FileClient fileClient(
//            CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry
//    ) {
//        return new FileClient("fileService", webClient(), circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
//    }

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
    public EmailUtils emailUtils(JavaMailSender jml) {

        return new EmailUtilsImpl(jml);
    }

}
