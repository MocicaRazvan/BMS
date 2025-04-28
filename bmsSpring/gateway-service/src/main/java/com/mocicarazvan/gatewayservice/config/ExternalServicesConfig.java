package com.mocicarazvan.gatewayservice.config;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URL;

@ConfigurationProperties(prefix = "spring.external-services")
@Component
@Data
@NoArgsConstructor
public class ExternalServicesConfig {
    private String diffusion;
    private String toxicity;
    private String umami;

    public void setDiffusion(String diffusion) {
        validateUrl(diffusion);
        this.diffusion = diffusion;
    }

    public void setToxicity(String toxicity) {
        validateUrl(toxicity);
        this.toxicity = toxicity;
    }

    public void setUmami(String umami) {
        validateUrl(umami);
        this.umami = umami;
    }

    private void validateUrl(String url) {
        try {
            URI uri = new URI(url);
            uri.toURL();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }
}
