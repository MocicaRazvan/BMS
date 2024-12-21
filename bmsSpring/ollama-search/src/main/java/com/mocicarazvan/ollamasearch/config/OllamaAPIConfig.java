package com.mocicarazvan.ollamasearch.config;


import io.github.ollama4j.OllamaAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
@RequiredArgsConstructor
public class OllamaAPIConfig {

    private final OllamaPropertiesConfig ollamaPropertiesConfig;

    @Bean
    @ConditionalOnMissingBean
    public OllamaAPI ollamaAPI() {
        return new OllamaAPI(ollamaPropertiesConfig.getUrl());
    }
}
