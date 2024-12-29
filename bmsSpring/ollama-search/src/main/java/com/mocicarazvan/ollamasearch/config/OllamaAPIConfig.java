package com.mocicarazvan.ollamasearch.config;


import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableRetry
public class OllamaAPIConfig {


    @Bean
    @ConditionalOnMissingBean(name = "ollamaAPIWebClient")
    public WebClient.Builder ollamaAPIWebClient() {
        return WebClient.builder();
    }
}
