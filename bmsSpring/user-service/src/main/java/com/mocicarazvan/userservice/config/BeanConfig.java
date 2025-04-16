package com.mocicarazvan.userservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.email.EmailUtils;
import com.mocicarazvan.templatemodule.email.config.MailConfig;
import com.mocicarazvan.templatemodule.email.impl.EmailUtilsImpl;
import com.mocicarazvan.templatemodule.hateos.user.PageableUserAssembler;
import com.mocicarazvan.templatemodule.hateos.user.UserDtoAssembler;
import com.mocicarazvan.templatemodule.jackson.CustomObjectMapper;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import com.mocicarazvan.templatemodule.utils.RepositoryUtils;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import com.mocicarazvan.userservice.controllers.UserControllerImpl;
import com.mocicarazvan.userservice.email.CustomMailProps;
import com.mocicarazvan.userservice.email.SecretProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BeanConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(authorizeExchangeSpec ->
                        authorizeExchangeSpec
                                .anyExchange().permitAll()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }


    @Bean
    public ObjectMapper customObjectMapper(final Jackson2ObjectMapperBuilder builder) {
        return new CustomObjectMapper(builder).customObjectMapper();
    }

    @Bean
    @Primary
    public WebClient.Builder webClient() {
        return WebClient.builder();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
    public PageableUserAssembler pageableUserAssembler() {
        return new PageableUserAssembler(new UserDtoAssembler(UserControllerImpl.class), UserControllerImpl.class);
    }

    @Bean
    public JavaMailSender javaMailSender(CustomMailProps customMailProps, SecretProperties secretProperties) throws Exception {
        return new MailConfig<>(customMailProps, secretProperties).javaMailSender();
    }

    @Bean
    public EmailUtils emailUtils(JavaMailSender jml) {

        return new EmailUtilsImpl(jml);
    }

    @Bean(name = "fileWebClient")
    @Profile("!k8s")
    @LoadBalanced
    public WebClient.Builder fileWebClient() {
        return WebClient.builder();
    }


    @Bean(name = "fileWebClient")
    @Profile("k8s")
    public WebClient.Builder fileWebClientk8s() {
        return WebClient.builder();
    }

    @Bean
    public FileClient fileClient(
            CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry,
            @Qualifier("fileWebClient") WebClient.Builder fileWebClient
    ) {
        return new FileClient("fileService", fileWebClient, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    @Bean
    public RepositoryUtils repositoryUtils() {
        return new RepositoryUtils();
    }


}
