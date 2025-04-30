package com.mocicarazvan.gatewayservice.config;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "spring.external-services")
@Component
@Data
@NoArgsConstructor
@Validated
public class ExternalServicesConfig {
    @URL(message = "Invalid URL format for diffusion service")
    private String diffusion;
    @URL(message = "Invalid URL format for toxicity service")
    private String toxicity;
    @URL(message = "Invalid URL format for time series service")
    private String timeSeries;
}
