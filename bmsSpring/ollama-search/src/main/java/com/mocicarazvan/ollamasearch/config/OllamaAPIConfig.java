package com.mocicarazvan.ollamasearch.config;


import io.github.ollama4j.OllamaAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class OllamaAPIConfig {

    @Value("${spring.custom.ollama.url}")
    private String url;

    @Bean
    @ConditionalOnMissingBean
    public OllamaAPI ollamaAPI() {
        return new OllamaAPI(url);
    }
}
