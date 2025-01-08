package com.mocicarazvan.gatewayservice.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "next.auth")
public class NextAuthProperties {
    private String secret;
    private boolean csrfEnabled;
}
