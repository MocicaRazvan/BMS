package com.mocicarazvan.gatewayservice.config;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.external-services")
@Component
@Data
@NoArgsConstructor
public class ExternalServicesConfig {
    private String diffusion;
    private String toxicity;
}
