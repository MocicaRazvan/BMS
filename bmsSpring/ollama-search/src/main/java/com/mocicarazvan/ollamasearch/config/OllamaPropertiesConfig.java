package com.mocicarazvan.ollamasearch.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.custom.ollama")
@Data
public class OllamaPropertiesConfig {
    private String url;
    private String embeddingModel;
    private String keepalive = "-1m";
    private int numCtx = 2048;


    private double threshold = 0.55;

}
